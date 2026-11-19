# AGENTS.md — Contexto do Projeto Sideboardy Play

Este repositório (`matheusrayol/sideboardy-play`) é um fork de [`arcanite24/playmat`](https://github.com/arcanite24/playmat) (baseado em [`magefree/mage`](https://github.com/magefree/mage) / XMage), criado para rodar a plataforma de partidas online em um **subdomínio exclusivo do Sideboardy** (ex.: `play.sideboardy.app`), de forma totalmente independente do gerenciador de decks e coleções (`Sideboardy`).

## Remotes Git configurados

- `origin`: `git@github.com:matheusrayol/sideboardy-play.git` (nosso repositório)
- `upstream`: `https://github.com/arcanite24/playmat.git` (atualizações da interface web `Mage.Web.Client`, ponte WebSocket e IA)
- `xmage`: `https://github.com/magefree/mage.git` (cartas novas e correções de regras do XMage oficial)

## Decisões já tomadas (contexto da sessão anterior)

1. **Separação de projetos:** O `Sideboardy` principal (`matheusrayol/sideboardy`) foi mantido focado exclusivamente em coleções e decks (sem Forge nem Playmat acoplados). Toda a evolução das mesas online acontece aqui no `sideboardy-play`.
2. **Sincronização com o XMage oficial (`xmage/master`):** O `arcanite24/playmat` faz merges periódicos do `magefree/mage` (o último foi em 2026-10-30). Podemos fazer merge de `xmage/master` diretamente aqui quando precisarmos de cartas novas e bugfixes recentes antes do `arcanite24`.
3. **Prioridades de customização em `Mage.Web.Client`:**
   - **Tradução PT-BR** completa (mesa, turnos, zonas, combate, atalhos e navegação).
   - **Visual imersivo de mesa real:** fundos de mesa/playmat realistas com zonas translúcidas e verso de carta personalizado.
   - **Deploy em subdomínio exclusivo** via Docker (`ops/web/docker-compose.yml` + proxy reverso).
   - **Integração leve com o Sideboardy:** importação de decks por link/texto e, numa etapa seguinte, SSO opcional.
   - **Chat de voz nas salas (WebRTC):** planejado para depois que a base visual e o deploy no subdomínio estiverem estáveis (o chat de texto já é nativo via `chatSendMessage`).

## Relação com o Sideboardy (`sideboardy.app`)

- **Repositório principal:** `matheusrayol/sideboardy` (local em `C:\Users\Matheus\OneDrive\Documentos\Projetos\Sideboardy`, memórias em `docs/agentes/memoria.md`).
- Quando for implementar integrações entre os dois projetos (ex.: importar deck público de `sideboardy.app` ou SSO/OAuth), adicione a pasta do `Sideboardy` ao workspace ou consulte a API pública/rotas de lá diretamente.

## Produção & Migração (`play.sideboardy.app`)

- **URL pública:** `https://play.sideboardy.app` (VPS `root@sideboardy.app`, diretório `/var/www/sideboardy-play`, projeto Compose `sideboardy-play`).
- **Modo de proxy:** Nginx do host terminando TLS (Certbot) e encaminhando para o Caddy interno do Compose em `http://10.89.17.10:80` (`TLS_MODE=external` + `ops/web/docker-compose.proxy.yml`).
- **Runbook completo (deploy, Nginx/Certbot, backup/restore e migração de servidor):** ver [docs/DEPLOY_MIGRACAO.md](docs/DEPLOY_MIGRACAO.md).

