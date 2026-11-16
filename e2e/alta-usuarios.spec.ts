import { expect, test } from '@playwright/test';

import { iniciarSesion } from './support/interfaz.js';

test('el coordinador crea tutor3 con el rol Tutor y lo ve en la gestión de usuarios', async ({ page }) => {
  await iniciarSesion(page, 'coordinador');
  const administracion = page.getByTestId('adminMenu');
  await administracion.getByRole('link', { name: 'Administración', exact: true }).click();
  await administracion.getByRole('link', { name: 'Gestión de usuarios', exact: true }).click();
  await expect(page.getByTestId('UserManagementHeading')).toBeVisible();
  await page.getByTestId('entityCreateButton').click();
  await expect(page.getByTestId('UserManagementCreateUpdateHeading')).toBeVisible();

  await page.getByTestId('login').fill('tutor3');
  await page.getByTestId('firstName').fill('Tutor');
  await page.getByTestId('lastName').fill('Tres');
  await page.getByTestId('email').fill('tutor3@sgpg.example');
  await page.getByTestId('langKey').selectOption('es');
  // Angular usa [ngValue]: el atributo value es interno ("3: 'ROLE_TUTOR'"), así que se selecciona por la etiqueta visible.
  await expect(page.getByTestId('authorities').getByRole('option', { name: 'Tutor', exact: true })).toBeAttached();
  await page.getByTestId('authorities').selectOption([{ label: 'Usuario' }, { label: 'Tutor' }]);
  await page.getByTestId('entityCreateSaveButton').click();
  await expect(page.getByTestId('UserManagementHeading')).toBeVisible();

  // Orden descendente por login: tutor3 queda en la primera página sin depender del total de usuarios.
  await page.goto('/user-management?sort=login,desc');
  const fila = page.getByTestId('entityTable').filter({ has: page.getByRole('link', { name: 'tutor3', exact: true }) });
  await expect(fila).toBeVisible();
  await expect(fila).toContainText('tutor3@sgpg.example');
  await expect(fila.getByRole('link', { name: 'Tutor', exact: true })).toHaveAttribute('href', '/authority/ROLE_TUTOR/view');
  await expect(fila.getByRole('link', { name: 'Usuario', exact: true })).toBeVisible();
});
