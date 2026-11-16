import { expect, test } from '@playwright/test';

import { proyectosDemo, usuariosDemo } from './support/datos-demo.js';
import { iniciarSesion, irAMisProyectos, tarjetaProyecto } from './support/interfaz.js';

test.describe('Permisos por rol y relación con el proyecto', () => {
  for (const usuario of usuariosDemo) {
    test(`el menú Entidades ${usuario === 'coordinador' ? 'aparece' : 'no aparece'} para ${usuario}`, async ({ page }) => {
      await iniciarSesion(page, usuario);
      const menu = page.getByTestId('entity');
      if (usuario === 'coordinador') {
        await expect(menu.getByRole('link', { name: 'Entidades', exact: true })).toBeVisible();
      } else {
        await expect(menu).toHaveCount(0);
      }
    });
  }

  test('estudiante1 ve únicamente P1 y P6 en Mis proyectos', async ({ page }) => {
    await iniciarSesion(page, 'estudiante1');
    const listado = await irAMisProyectos(page);

    await expect(listado.getByRole('article')).toHaveCount(2);
    await expect(tarjetaProyecto(page, proyectosDemo.P1.titulo)).toBeVisible();
    await expect(tarjetaProyecto(page, proyectosDemo.P6.titulo)).toBeVisible();
  });

  test('jurado3 recibe un mensaje de permiso al abrir directamente P3', async ({ page }) => {
    await iniciarSesion(page, 'jurado3');
    await irAMisProyectos(page);
    await expect(tarjetaProyecto(page, proyectosDemo.P3.titulo)).toHaveCount(0);

    // Una URL conocida también debe respetar los permisos del servidor.
    await page.goto(`/seguimiento/${String(proyectosDemo.P3.id)}`);
    await expect(page.locator('jhi-seguimiento-alert').getByRole('alert')).toContainText(
      'No tiene permisos para realizar esta acción o consultar este proyecto.',
    );
    await expect(page.getByRole('heading', { name: proyectosDemo.P3.titulo, exact: true })).toHaveCount(0);
  });
});
