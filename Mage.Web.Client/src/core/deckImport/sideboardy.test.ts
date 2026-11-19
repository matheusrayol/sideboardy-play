// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import {
  fetchSideboardyStaffRole,
  forgotPasswordSideboardy,
  loginSideboardy,
  registerSideboardy,
  syncSideboardySso,
  toPlayUsername,
} from './sideboardy';

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  });
}

describe('sideboardy auth & sso helpers', () => {
  const fetchSpy = vi.fn<typeof fetch>();

  beforeEach(() => {
    fetchSpy.mockReset();
    vi.stubGlobal('fetch', fetchSpy);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  test('toPlayUsername normalizes names for XMage seats', () => {
    expect(toPlayUsername('Matheus.Rayol', 1)).toBe('matheus_rayol');
    expect(toPlayUsername('ab', 42)).toBe('sb_ab_42');
    expect(toPlayUsername('admin', 7)).toBe('sb_7');
    expect(toPlayUsername('VeryLongUsernameHere', 3)).toBe('verylonguserna');
  });

  test('loginSideboardy posts to /sideboardy-api/auth/login and resolves playUsername from SSO', async () => {
    fetchSpy
      .mockResolvedValueOnce(jsonResponse({ user: { id: 10, username: 'alice' } }))
      .mockResolvedValueOnce(jsonResponse({ authenticated: true, playUsername: 'alice' }));

    const playUser = await loginSideboardy('alice@example.com', 'secret123');
    expect(playUser).toBe('alice');
    expect(fetchSpy).toHaveBeenNthCalledWith(
      1,
      '/sideboardy-api/auth/login',
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({ login: 'alice@example.com', password: 'secret123' }),
      }),
    );
    expect(fetchSpy).toHaveBeenNthCalledWith(
      2,
      '/sideboardy-api/auth/sso',
      expect.objectContaining({ credentials: 'include', cache: 'no-store' }),
    );
  });

  test('loginSideboardy throws the backend error when credentials are invalid', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ error: 'Usuário ou senha incorretos.' }, 401));
    await expect(loginSideboardy('alice', 'wrong')).rejects.toThrow('Usuário ou senha incorretos.');
  });

  test('registerSideboardy creates account on Sideboardy with policies_read and resolves playUsername', async () => {
    fetchSpy
      .mockResolvedValueOnce(jsonResponse({ user: { id: 25, username: 'novato' } }, 201))
      .mockResolvedValueOnce(jsonResponse({ authenticated: true, playUsername: 'novato' }));

    const playUser = await registerSideboardy('novato', 'novato@example.com', 'secret123');
    expect(playUser).toBe('novato');
    expect(fetchSpy).toHaveBeenNthCalledWith(
      1,
      '/sideboardy-api/auth/register',
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({
          username: 'novato',
          email: 'novato@example.com',
          password: 'secret123',
          display_name: 'novato',
          policies_read: true,
        }),
      }),
    );
  });

  test('forgotPasswordSideboardy forwards recovery emails to Sideboardy', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ message: 'Link enviado.' }));
    await expect(forgotPasswordSideboardy('user@example.com')).resolves.toBe('Link enviado.');
    expect(fetchSpy).toHaveBeenCalledWith(
      '/sideboardy-api/auth/forgot-password',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ email: 'user@example.com' }),
      }),
    );
  });

  test('syncSideboardySso reports reachable when JSON /auth/sso responds unauthenticated', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ authenticated: false, playUsername: null }));
    await expect(syncSideboardySso()).resolves.toEqual({
      authenticated: false,
      playUsername: null,
      reachable: true,
    });
  });

  test('fetchSideboardyStaffRole resolves admin, moderator, and null from /sideboardy-api/auth/me', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ user: { id: 2, username: 'matheus', is_admin: 1, role: 'admin' } }));
    await expect(fetchSideboardyStaffRole()).resolves.toBe('admin');

    fetchSpy.mockResolvedValueOnce(jsonResponse({ user: { id: 3, username: 'mod', is_admin: 0, is_moderator: 1, role: 'moderator' } }));
    await expect(fetchSideboardyStaffRole()).resolves.toBe('moderator');

    fetchSpy.mockResolvedValueOnce(jsonResponse({ user: { id: 4, username: 'player', is_admin: 0, is_moderator: 0, role: 'user' } }));
    await expect(fetchSideboardyStaffRole()).resolves.toBeNull();
  });
});
