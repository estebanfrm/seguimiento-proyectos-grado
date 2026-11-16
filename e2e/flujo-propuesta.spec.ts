import { expect, test } from '@playwright/test';

import { proyectosDemo } from './support/datos-demo.js';
import { abrirSeguimiento, iniciarSesion } from './support/interfaz.js';

test('tutor1 avanza P2 de PROPUESTA a ANTEPROYECTO y conserva el historial', async ({ page }) => {
  await iniciarSesion(page, 'tutor1');
  await abrirSeguimiento(page, proyectosDemo.P2.titulo);

  const etapas = page.getByRole('list', { name: 'Etapas del proyecto', exact: true });
  await expect(etapas.locator('[aria-current="step"]')).toContainText('Propuesta');
  const requisitos = page.getByRole('region', { name: 'Requisitos para avanzar', exact: true });
  await expect(requisitos.locator('[data-requisito]')).toHaveCount(4);
  for (const codigo of ['TUTOR_ASIGNADO', 'ESTUDIANTES_ASIGNADOS', 'ENTREGA_APROBADA', 'SIN_CORRECCIONES_PENDIENTES']) {
    await expect(requisitos.locator(`[data-requisito="${codigo}"]`)).toContainText('✔');
  }

  await expect(page.getByTestId('avanzar')).toBeEnabled();
  await page.getByTestId('avanzar').click();
  const dialogo = page.getByRole('dialog', { name: 'Avanzar etapa', exact: true });
  const comentario = 'E2E: propuesta aprobada y requisitos verificados por tutor1.';
  await dialogo.getByLabel('Comentario', { exact: true }).fill(comentario);
  await dialogo.getByRole('button', { name: 'Guardar', exact: true }).click();
  await expect(dialogo).toBeHidden();

  await expect(etapas.locator('[aria-current="step"]')).toContainText('Anteproyecto');
  await expect(etapas.getByRole('listitem').filter({ has: page.getByText('Propuesta', { exact: true }) })).toHaveAttribute(
    'data-estado',
    'COMPLETADA',
  );
  const evento = page
    .getByRole('region', { name: 'Historial del proyecto', exact: true })
    .getByRole('listitem')
    .filter({ hasText: comentario });
  await expect(evento).toHaveCount(1);
  await expect(evento).toContainText('Propuesta → Anteproyecto');
  await expect(evento).toContainText('tutor1');

  await page.reload();
  await expect(etapas.locator('[aria-current="step"]')).toContainText('Anteproyecto');
  await expect(evento).toHaveCount(1);
});
