import { expect, test } from '@playwright/test';

import { proyectosDemo } from './support/datos-demo.js';
import { abrirSeguimiento, iniciarSesion } from './support/interfaz.js';

test.describe('Requisitos y fecha vencida de P4', () => {
  test('tutor2 encuentra el avance bloqueado y los requisitos pendientes', async ({ page }) => {
    await iniciarSesion(page, 'tutor2');
    await abrirSeguimiento(page, proyectosDemo.P4.titulo);

    // La UI impide abrir la confirmación cuando faltan requisitos; no se fuerza el clic.
    await expect(page.getByTestId('avanzar')).toBeDisabled();
    const requisitos = page.getByRole('region', { name: 'Requisitos para avanzar', exact: true });
    await expect(requisitos.locator('[data-requisito]')).toHaveCount(3);
    await expect(requisitos.locator('[data-requisito="ENTREGA_APROBADA"]')).toContainText('✘');
    await expect(requisitos.locator('[data-requisito="SIN_CORRECCIONES_PENDIENTES"]')).toContainText('✘');
    await expect(requisitos.locator('[data-requisito="SUSTENTACION_PROGRAMADA"]')).toContainText('✔');
    await expect(page.getByRole('list', { name: 'Etapas del proyecto', exact: true }).locator('[aria-current="step"]')).toContainText(
      'Desarrollo',
    );
  });

  test('estudiante4 no puede crear una entrega porque el plazo de Desarrollo venció', async ({ page }) => {
    await iniciarSesion(page, 'estudiante4');
    await abrirSeguimiento(page, proyectosDemo.P4.titulo);

    const fechas = page.getByRole('region', { name: 'Fechas límite', exact: true });
    await expect(fechas.getByText('Desarrollo', { exact: true }).locator('..')).toContainText('VENCIDA');
    await expect(page.getByTestId('nueva-entrega')).toBeDisabled();
    await expect(
      page.getByText('No puede crear otra entrega si venció el plazo o ya hay una entrega enviada, en revisión o aprobada en esta etapa.', {
        exact: true,
      }),
    ).toBeVisible();
    await expect(page.getByRole('dialog')).toHaveCount(0);
  });
});
