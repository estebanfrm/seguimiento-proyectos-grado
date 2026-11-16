import { expect, test } from '@playwright/test';

import { iniciarSesion, irAMisProyectos, tarjetaProyecto } from './support/interfaz.js';

test('estudiante2 registra una propuesta y la ve en su lista en PROPUESTA con 0 %', async ({ page }) => {
  await iniciarSesion(page, 'estudiante2');
  const listado = await irAMisProyectos(page);
  const cantidadInicial = await listado.getByRole('article').count();
  const titulo = 'E2E - Plataforma de acompañamiento de proyectos estudiantiles';
  await expect(tarjetaProyecto(page, titulo)).toHaveCount(0);
  await page.getByTestId('registrar-propuesta').click();

  const dialogo = page.getByRole('dialog', { name: 'Registrar propuesta', exact: true });
  await dialogo.getByLabel('Título', { exact: true }).fill(titulo);
  await dialogo
    .getByLabel('Resumen', { exact: true })
    .fill('Propuesta registrada desde el navegador por estudiante2 para verificar el flujo académico.');
  await dialogo.getByLabel('Programa', { exact: true }).fill('Ingeniería de Sistemas');
  await dialogo.getByRole('button', { name: 'Guardar', exact: true }).click();
  await expect(dialogo).toBeHidden();

  const tarjeta = tarjetaProyecto(page, titulo);
  await expect(listado.getByRole('article')).toHaveCount(cantidadInicial + 1);
  await expect(tarjeta).toBeVisible();
  await expect(tarjeta.getByText('Propuesta', { exact: true })).toBeVisible();
  await expect(tarjeta.getByText('Activo', { exact: true })).toBeVisible();
  await expect(tarjeta.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0');
  await expect(tarjeta.getByText('0%', { exact: true })).toBeVisible();

  await page.reload();
  await expect(tarjeta).toBeVisible();
  await expect(tarjeta.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0');
});
