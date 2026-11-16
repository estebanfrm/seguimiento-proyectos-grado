// Títulos e identificadores de config/liquibase/data/demo/proyecto_grado.csv.
export const proyectosDemo = {
  P1: { id: 2001, titulo: 'P1 - Monitoreo del consumo de agua en el campus' },
  P2: { id: 2002, titulo: 'P2 - Biblioteca digital accesible para estudiantes' },
  P3: { id: 2003, titulo: 'P3 - Predicción de demanda del transporte universitario' },
  P4: { id: 2004, titulo: 'P4 - Plataforma de inventario de laboratorios' },
  P5: { id: 2005, titulo: 'P5 - Sistema de alertas tempranas de deserción' },
  P6: { id: 2006, titulo: 'P6 - Portal de prácticas académicas y seguimiento' },
} as const;

export const usuariosDemo = [
  'coordinador',
  'estudiante1',
  'estudiante2',
  'estudiante3',
  'estudiante4',
  'estudiante5',
  'tutor1',
  'tutor2',
  'jurado1',
  'jurado2',
  'jurado3',
] as const;

export type UsuarioDemo = (typeof usuariosDemo)[number];
