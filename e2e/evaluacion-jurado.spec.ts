import { expect, test } from '@playwright/test';

import { proyectosDemo } from './support/datos-demo.js';
import { abrirSeguimiento, cerrarSesion, iniciarSesion } from './support/interfaz.js';

// Las dos sesiones forman un único flujo: ningún otro spec necesita el estado inicial de P3.
test('jurado2 corrige una evaluación incoherente de P3 y tutor2 puede avanzar', async ({ page }) => {
  await iniciarSesion(page, 'jurado2');
  await abrirSeguimiento(page, proyectosDemo.P3.titulo);
  await expect(page.locator('[data-requisito="EVALUACIONES_COMPLETAS"]')).toContainText('✘');
  await page.getByTestId('evaluar').click();

  const dialogo = page.getByRole('dialog', { name: 'Evaluar', exact: true });
  await dialogo.getByLabel('Calificación (0 a 5)', { exact: true }).fill('2.5');
  await dialogo.getByLabel('Concepto', { exact: true }).selectOption('APROBADO');
  await expect(dialogo.locator('#evaluacion-hint')).toContainText('Una calificación menor de 3.0 exige Reprobado');
  await expect(dialogo.locator('#evaluacion-hint')).toHaveClass(/text-danger/);
  await expect(dialogo.getByRole('button', { name: 'Guardar', exact: true })).toBeDisabled();

  await dialogo.getByLabel('Calificación (0 a 5)', { exact: true }).fill('4.0');
  const comentario = 'E2E: anteproyecto aprobado por jurado2 con calificación 4.0.';
  await dialogo.getByLabel('Comentarios', { exact: true }).fill(comentario);
  await expect(dialogo.getByRole('button', { name: 'Guardar', exact: true })).toBeEnabled();
  await dialogo.getByRole('button', { name: 'Guardar', exact: true }).click();
  await expect(dialogo).toBeHidden();

  const evaluacion = page.getByRole('region', { name: 'Anteproyecto', exact: true }).getByRole('listitem').filter({ hasText: comentario });
  await expect(evaluacion).toHaveCount(1);
  await expect(evaluacion).toContainText('jurado2 · 4 / 5 · Aprobado');
  await expect(page.getByTestId('evaluar')).toHaveCount(0);

  await cerrarSesion(page);
  await iniciarSesion(page, 'tutor2');
  await abrirSeguimiento(page, proyectosDemo.P3.titulo);
  await expect(page.locator('[data-requisito="EVALUACIONES_COMPLETAS"]')).toContainText('✔');
  await expect(page.locator('[data-requisito="EVALUACION_APROBATORIA"]')).toContainText('✔');
  await expect(page.getByTestId('avanzar')).toBeEnabled();
  await expect(evaluacion).toHaveCount(1);
});
