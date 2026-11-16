import { expect, type Locator, type Page } from '@playwright/test';

import type { UsuarioDemo } from './datos-demo.js';

export async function iniciarSesion(page: Page, usuario: UsuarioDemo): Promise<void> {
  await page.goto('/login');
  await expect(page.getByTestId('loginTitle')).toBeVisible();
  await page.getByTestId('username').fill(usuario);
  await page.getByTestId('password').fill('user');
  await page.getByTestId('submit').click();
  await expect(page.getByTestId('mis-proyectos')).toBeVisible();
  await expect(page.getByTestId('loginTitle')).toHaveCount(0);
}

export async function cerrarSesion(page: Page): Promise<void> {
  await page.getByTestId('accountMenu').getByRole('link', { name: 'Cuenta', exact: true }).click();
  await page.getByTestId('logout').click();
  await expect(page.getByTestId('mis-proyectos')).toHaveCount(0);
  await expect(page.getByTestId('login')).toHaveCount(1);
}

export async function irAMisProyectos(page: Page): Promise<Locator> {
  await page.getByTestId('mis-proyectos').click();
  const listado = page.getByRole('region', { name: 'Mis proyectos', exact: true });
  await expect(listado).toBeVisible();
  await expect(listado.locator('[aria-busy]')).toHaveAttribute('aria-busy', 'false');
  await expect(listado.getByRole('alert')).toHaveCount(0);
  return listado;
}

export function tarjetaProyecto(page: Page, titulo: string): Locator {
  return page.getByRole('article').filter({ has: page.getByRole('heading', { name: titulo, exact: true }) });
}

export async function abrirSeguimiento(page: Page, titulo: string): Promise<void> {
  await irAMisProyectos(page);
  await tarjetaProyecto(page, titulo).getByRole('link', { name: 'Ver seguimiento', exact: true }).click();
  await expect(page.getByRole('heading', { level: 1, name: titulo, exact: true })).toBeVisible();
  await expect(page.getByRole('list', { name: 'Etapas del proyecto', exact: true }).getByRole('listitem')).toHaveCount(5);
}
