import { expect, test } from '@playwright/test';

import { proyectosDemo } from './support/datos-demo.js';
import { abrirSeguimiento, iniciarSesion, irAMisProyectos, tarjetaProyecto } from './support/interfaz.js';

test('tutor1 finaliza P5 con estado APROBADO y avance 100 %', async ({ page }) => {
  await iniciarSesion(page, 'tutor1');
  await abrirSeguimiento(page, proyectosDemo.P5.titulo);
  const etapas = page.getByRole('list', { name: 'Etapas del proyecto', exact: true });
  await expect(etapas.locator('[aria-current="step"]')).toContainText('Sustentación');
  await expect(page.getByRole('progressbar', { name: 'Avance', exact: true })).toHaveAttribute('aria-valuenow', '87');
  await expect(page.getByTestId('avanzar')).toBeEnabled();

  await page.getByTestId('avanzar').click();
  const dialogo = page.getByRole('dialog', { name: 'Avanzar etapa', exact: true });
  const comentario = 'E2E: cierre académico aprobado por tutor1.';
  await dialogo.getByLabel('Comentario', { exact: true }).fill(comentario);
  await dialogo.getByRole('button', { name: 'Guardar', exact: true }).click();
  await expect(dialogo).toBeHidden();

  // El seguimiento marca todas las etapas como COMPLETADA al finalizar.
  await expect(etapas.locator('[data-estado="COMPLETADA"]')).toHaveCount(5);
  await expect(etapas.getByRole('listitem').filter({ has: page.getByText('Finalizado', { exact: true }) })).toHaveAttribute(
    'data-estado',
    'COMPLETADA',
  );
  await expect(page.getByRole('progressbar', { name: 'Avance', exact: true })).toHaveAttribute('aria-valuenow', '100');
  const detalle = page.getByRole('region', { name: proyectosDemo.P5.titulo, exact: true });
  await expect(detalle.locator('header').getByText('Aprobado', { exact: true })).toBeVisible();
  await expect(page.getByTestId('avanzar')).toHaveCount(0);
  const evento = page
    .getByRole('region', { name: 'Historial del proyecto', exact: true })
    .getByRole('listitem')
    .filter({ hasText: comentario });
  await expect(evento).toContainText('Sustentación → Finalizado');
  await expect(evento).toContainText('tutor1');

  await irAMisProyectos(page);
  const tarjeta = tarjetaProyecto(page, proyectosDemo.P5.titulo);
  await expect(tarjeta.getByText('Finalizado', { exact: true })).toBeVisible();
  await expect(tarjeta.getByText('Aprobado', { exact: true })).toBeVisible();
  await expect(tarjeta.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '100');
});
