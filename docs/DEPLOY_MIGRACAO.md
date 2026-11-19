# Guia de Deploy e Migração de Servidor — Sideboardy Play (`play.sideboardy.app`)

Este documento registra o passo a passo exato executado para colocar o **Sideboardy Play** no ar em **`https://play.sideboardy.app`**, servindo de runbook reproduzível para futuras **migrações de servidor**, atualizações e recuperação de desastres.

---

## 1. Arquitetura em Produção

- **Host atual:** `root@sideboardy.app` (`x86_64`, Ubuntu, Nginx + Certbot na porta `80`/`443`)
- **Diretório na VPS:** `/var/www/sideboardy-play`
- **Projeto Docker Compose:** `sideboardy-play` (`-p sideboardy-play`)
- **Fluxo de tráfego:**

```text
Internet (https://play.sideboardy.app)
   └─► Cloudflare DNS (A -> IP da VPS, DNS only durante emissão ACME)
         └─► Host Nginx (:80 / :443 com TLS Certbot)
               └─► Docker Caddy (:80 no IP 10.89.17.10 da rede xmage; TLS_MODE=external)
                     ├─ /       ──► Arquivos estáticos do Mage.Web.Client (/srv)
                     ├─ /ws*    ──► sideboardy-play-xmage-1:17172 (WebSocket XMage)
                     ├─ /img/*  ──► sideboardy-play-imgcache-1:8080 (cache nginx para Scryfall)
                     └─ /playgroup-api/* ──► proxy para https://playgroup.gg/api/public/v1
```

### Containers da stack (`sideboardy-play`)

| Container | Imagem | Função |
|---|---|---|
| `sideboardy-play-xmage-1` | `xmage-web-server:local` | Servidor XMage (`17172` WebSocket, `17173` `/healthz` interno) |
| `sideboardy-play-web-1` | `xmage-web:local` | Caddy servindo o front Vite + roteamento `/ws` e `/img` |
| `sideboardy-play-imgcache-1` | `nginx:1.30.5-alpine` | Cache local de imagens e consultas da API do Scryfall |
| `sideboardy-play-backup-1` | `xmage-web-server:local` | Daemon diário de backup (`xmage-backup`) às `03:30 UTC` |

### Volumes Docker persistentes

- `sideboardy-play_xmage-db`: contas, estatísticas, decks sincronizados, histórico de partidas e admin (SQLite/H2). **Este é o volume essencial para migrações.**
- `sideboardy-play_xmage-backups`: dumps gerados pelo `xmage-backup` (retenção padrão de `14` backups).
- `sideboardy-play_xmage-cards`: cache do banco de cartas (`cards.h2.mv.db` e `catalog.sqlite`), substituído automaticamente a cada rebuild pelo `.build-id` da imagem.
- `sideboardy-play_xmage-saved`, `sideboardy-play_xmage-logs`, `sideboardy-play_xmage-updates`, `sideboardy-play_xmage-imgcache`, `sideboardy-play_caddy-data`, `sideboardy-play_caddy-config`.

---

## 2. Pré-requisitos em um Novo Servidor

Antes de iniciar o deploy ou migração em uma nova VPS Linux:

1. **DNS (Cloudflare ou provedor equivalente):**
   - Criar/atualizar o registro `A` de `play.sideboardy.app` apontando para o IPv4 do novo servidor (manter em **DNS only / nuvem cinza** enquanto emitir o certificado Let's Encrypt; se usar proxy laranja depois, seguir `ops/web/CLOUDFLARE.md`).
2. **Pacotes no host:**
   - Docker Engine com plugin `docker compose` (`v2.24+`, necessário para suportar `ports: !reset []` em `ops/web/docker-compose.proxy.yml`)
   - `nginx`
   - `certbot` e `python3-certbot-nginx`
3. **Rede Docker externa compartilhada (`proxy`):**
   ```bash
   docker network inspect proxy >/dev/null 2>&1 || docker network create proxy
   ```
4. **Firewall:** portas `80/tcp` e `443/tcp` abertas publicamente.

---

## 3. Passo a Passo Executado (Deploy do Zero ou em Novo Servidor)

### Passo 1 — Limpar vestígios antigos (se houver) e criar o diretório
```bash
ssh root@SEU_SERVIDOR "
  docker network inspect proxy >/dev/null 2>&1 || docker network create proxy
  mkdir -p /var/www/sideboardy-play
"
```

### Passo 2 — Enviar o código do projeto para `/var/www/sideboardy-play`

**Opção A — Via Git (recomendado quando o código estiver no repositório remoto):**
```bash
git clone git@github.com:matheusrayol/sideboardy-play.git /var/www/sideboardy-play
```

**Opção B — Envio direto do workspace local Windows via `tar` + `ssh` (método usado no deploy inicial):**
> No Windows PowerShell, use `cmd /c` no pipeline para evitar que o PowerShell altere a stream binária do `.tar.gz`, e grave `.git/HEAD` para o servidor registrar o commit no `/healthz`.

```powershell
$commit = (git rev-parse HEAD).Trim()
cmd /c "tar -czf - --exclude=.git --exclude=Mage.Web.Client/node_modules --exclude=Mage.Web.Client/dist --exclude=rust/target --exclude=Utils/data --exclude=ops/web/.env . | ssh -o BatchMode=yes root@SEU_SERVIDOR `"rm -rf /var/www/sideboardy-play/* /var/www/sideboardy-play/.dockerignore /var/www/sideboardy-play/.git && tar -xzf - -C /var/www/sideboardy-play && mkdir -p /var/www/sideboardy-play/.git && echo $commit > /var/www/sideboardy-play/.git/HEAD && chmod -R u+w /var/www/sideboardy-play`""
```

### Passo 3 — Configurar `/var/www/sideboardy-play/ops/web/.env` na VPS

Crie o arquivo `.env` no servidor ajustando as credenciais conforme o ambiente:

```bash
cat <<'EOF' > /var/www/sideboardy-play/ops/web/.env
DOMAIN=play.sideboardy.app
XMAGE_SERVER_IMAGE=
XMAGE_WEB_IMAGE=
TLS_MODE=external
PROXY_NETWORK=proxy
UPSTREAM_PROXIES=private_ranges
AUTH_TRUSTED_HEADER=X-Sideboardy-User
AUTH_TRUSTED_PROXIES=private_ranges
XMAGE_ALLOWED_ORIGINS=https://play.sideboardy.app
XMAGE_PUBLIC_URL=https://play.sideboardy.app
XMAGE_OWNER=matheus
XMAGE_ADMIN_PASSWORD=TROCAR_POR_SENHA_FORTE
XMAGE_AUTH=true
XMAGE_PASSWORD_SIGNIN=false
XMAGE_CAREER=off
XMAGE_ACCESS=open
EOF
```

Notas importantes sobre essas variáveis:
- `XMAGE_SERVER_IMAGE=` e `XMAGE_WEB_IMAGE=` vazios instruem o Compose a usar/construir as imagens locais (`xmage-web-server:local` e `xmage-web:local`).
- `TLS_MODE=external` desativa o TLS automático do Caddy interno para que o `nginx` do host controle os certificados nas portas `80`/`443`.
- `XMAGE_OWNER=matheus` concede o papel máximo (`owner`) à conta administradora sincronizada do Sideboardy, liberando o painel administrativo (`/admin`) e permitindo gerenciar papéis adicionais (`admin` / `moderator`) pela aba Equipe.
- `AUTH_TRUSTED_HEADER=X-Sideboardy-User`, `AUTH_TRUSTED_PROXIES=private_ranges`, `XMAGE_AUTH=true` e `XMAGE_PASSWORD_SIGNIN=false` habilitam o login unificado exclusivo via `sideboardy.app`: quem já estiver logado em `sideboardy.app` entra automaticamente pelo cookie `.sideboardy.app` validado via `auth_request`, e quem abrir `play.sideboardy.app` deslogado faz login, cadastro ou recuperação de senha diretamente na API do `sideboardy.app` (`/sideboardy-api/auth/*`) antes de autenticar a conexão WebSocket via `X-Sideboardy-User`, sem contas paralelas no XMage.

### Passo 4 — Construir as imagens e subir a stack Docker Compose

```bash
cd /var/www/sideboardy-play
docker compose -p sideboardy-play \
  -f ops/web/docker-compose.yml \
  -f ops/web/docker-compose.proxy.yml \
  --env-file ops/web/.env \
  up -d --build
```

> **Dica para VPS com pouca RAM/CPU:** se o novo servidor for pequeno (`1 vCPU / 1 GB`), construa as imagens na sua máquina local (mesma arquitetura `amd64` ou com `--platform`) e envie por SSH com `ops/web/ship-images.sh root@SEU_SERVIDOR` ou `docker save xmage-web-server:local xmage-web:local | gzip -1 | ssh ... "gunzip | docker load"`, subindo depois com `up -d --no-build`.

Valide internamente antes de expor no Nginx:
```bash
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}'
curl -sI -H 'Host: play.sideboardy.app' http://10.89.17.10:80/
```

### Passo 5 — Configurar o Nginx do host e emitir o certificado SSL (Certbot)

Crie `/etc/nginx/sites-available/play_sideboardy_app.conf` (os blocos `auth_request /_sideboardy_sso` e `/sideboardy-api/` conectam `play.sideboardy.app` ao backend do Sideboardy em `127.0.0.1:3001` para SSO automático via cookie `.sideboardy.app` e importação direta de decks; em seguida, o `certbot --nginx` adiciona o bloco TLS na porta `443` e o redirect da porta `80`):

```nginx
map $http_upgrade $playmat_connection_upgrade {
    default upgrade;
    ''      close;
}

server {
    listen 80;
    listen [::]:80;
    server_name play.sideboardy.app;

    client_max_body_size 10M;

    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    location = /_sideboardy_sso {
        internal;
        proxy_pass http://127.0.0.1:3001/api/auth/sso;
        proxy_pass_request_body off;
        proxy_set_header Content-Length "";
        proxy_set_header Host $host;
        proxy_set_header X-Original-URI $request_uri;
        proxy_set_header Cookie $http_cookie;
    }

    location /sideboardy-api/ {
        proxy_pass http://127.0.0.1:3001/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        auth_request /_sideboardy_sso;
        auth_request_set $sb_user $upstream_http_x_sideboardy_user;
        proxy_pass http://10.89.17.10:80;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Sideboardy-User $sb_user;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $playmat_connection_upgrade;
        proxy_read_timeout 1h;
        proxy_send_timeout 1h;
    }
}
```

Ative o site, valide a sintaxe, recarregue o Nginx e gere o certificado SSL com redirecionamento HTTPS:

```bash
ln -sf /etc/nginx/sites-available/play_sideboardy_app.conf /etc/nginx/sites-enabled/play_sideboardy_app.conf
nginx -t && systemctl reload nginx
certbot --nginx -d play.sideboardy.app --non-interactive --agree-tos --redirect
```

---

## 4. Checklist de Validação End-to-End

Execute os comandos abaixo após qualquer deploy ou migração:

1. **Containers e logs sem erros de cartas:**
   ```bash
   docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}'
   docker logs sideboardy-play-xmage-1 2>&1 | grep -E 'FATAL|Error loading card|Database stats|sets:|cards:|Started MAGE|installing'
   ```
   Esperado: nenhum `FATAL` / `Error loading card` e `xmage`, `web`, `imgcache` no estado `(healthy)`.

2. **Healthcheck interno do servidor XMage:**
   ```bash
   docker exec sideboardy-play-xmage-1 bash -c 'curl -sS http://127.0.0.1:17173/healthz'
   ```
   Esperado: `{"status":"ok",...}`.

3. **HTTPS, Redirecionamento HTTP -> HTTPS, WebSocket `/ws` e Proxy `/img`:**
   ```bash
   curl -sSI https://play.sideboardy.app/
   curl -sSI http://play.sideboardy.app/
   curl -sSi --http1.1 --max-time 3 -N \
     -H "Connection: Upgrade" \
     -H "Upgrade: websocket" \
     -H "Origin: https://play.sideboardy.app" \
     -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" \
     -H "Sec-WebSocket-Version: 13" \
     https://play.sideboardy.app/ws
   curl -sSI "https://play.sideboardy.app/img/api/cards/named?exact=Black+Lotus"
   ```
   Esperados: `200 OK`, `301 Moved Permanently`, `101 Switching Protocols` e `200 OK`.

4. **SSO (`/sideboardy-api/auth/sso`) e Proxy da API do Sideboardy:**
   ```bash
   curl -sSi https://play.sideboardy.app/sideboardy-api/auth/sso
   docker logs sideboardy-play-xmage-1 2>&1 | grep -i "single sign-on"
   ```
   Esperados: `HTTP/1.1 200 OK` com JSON válido (`{"authenticated":false,...}` quando sem cookie `sb_access`) e log `WebSocket: single sign-on only by the X-Sideboardy-User header from trusted proxies` no `sideboardy-play-xmage-1`. Com um cookie `sb_access` válido do domínio `.sideboardy.app`, `/sideboardy-api/auth/sso` retorna `X-Sideboardy-User: <playUsername>` e `/ws` responde `signOn: "<playUsername>"` no `serverInfo`.

---

## 5. Migração de Dados Entre Servidores (Backup & Restore)

Para migrar contas, estatísticas, decks e dados administrativos de uma VPS antiga para uma nova sem perda de estado:

### Na VPS antiga (gerar snapshot consistente)
1. Tire um backup online via `xmage-backup once` (definido em `ops/web/backup.sh`):
   ```bash
   docker exec sideboardy-play-backup-1 xmage-backup once
   docker exec sideboardy-play-backup-1 xmage-backup list
   ```
2. Exporte o volume de backups (`sideboardy-play_xmage-backups`) e o `.env`:
   ```bash
   docker run --rm -v sideboardy-play_xmage-backups:/backups -v /tmp:/out alpine \
     tar -czf /out/sideboardy-play-backups.tar.gz -C /backups .
   cp /var/www/sideboardy-play/ops/web/.env /tmp/sideboardy-play.env
   ```
   *(Opcionalmente, após parar os containers com `docker compose ... down`, também é possível clonar diretamente todo o volume `sideboardy-play_xmage-db` via `tar -czf`.)*

### Na VPS nova (restaurar dados)
1. Execute os **Passos 1 a 4 da Seção 3** para criar a stack e os volumes.
2. Copie `sideboardy-play-backups.tar.gz` para a nova VPS e importe para `sideboardy-play_xmage-backups`:
   ```bash
   docker run --rm -v sideboardy-play_xmage-backups:/backups -v /tmp:/in alpine \
     tar -xzf /in/sideboardy-play-backups.tar.gz -C /backups
   ```
3. Pare apenas o serviço `xmage`, restaure o snapshot desejado pelo nome retornado no `list` e suba novamente:
   ```bash
   cd /var/www/sideboardy-play
   docker compose -p sideboardy-play -f ops/web/docker-compose.yml -f ops/web/docker-compose.proxy.yml --env-file ops/web/.env stop xmage
   docker compose -p sideboardy-play -f ops/web/docker-compose.yml -f ops/web/docker-compose.proxy.yml --env-file ops/web/.env run --rm --no-deps backup xmage-backup restore NOME_DO_BACKUP
   docker compose -p sideboardy-play -f ops/web/docker-compose.yml -f ops/web/docker-compose.proxy.yml --env-file ops/web/.env up -d
   ```
4. Prossiga para o **Passo 5 (Nginx + Certbot)**, troque o registro `A` no Cloudflare para o IP da nova VPS e execute a **Seção 4 (Validação End-to-End)**.

---

## 6. Operação Contínua

- **Alterar apenas variáveis em `/var/www/sideboardy-play/ops/web/.env`** (ex.: trocar `XMAGE_ADMIN_PASSWORD` ou ativar `XMAGE_AUTH=true`) **não exige rebuild**:
  ```bash
  cd /var/www/sideboardy-play
  docker compose -p sideboardy-play -f ops/web/docker-compose.yml -f ops/web/docker-compose.proxy.yml --env-file ops/web/.env up -d
  ```
- **Atualizar código / cartas novas após mudanças no repositório:** sincronizar o diretório (via `git pull` ou `tar | ssh`) e rodar `up -d --build`. O `.build-id` da nova imagem atualiza automaticamente o banco pré-construído de cartas (`cards.h2.mv.db`) preservando o volume de usuários (`sideboardy-play_xmage-db`).
- **Acompanhar logs:**
  ```bash
  docker logs -f --tail 100 sideboardy-play-xmage-1
  docker logs -f --tail 100 sideboardy-play-web-1
  ```
