import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

import { defineConfig, devices } from '@playwright/test';

const raiz = fileURLToPath(new URL('..', import.meta.url));

export default defineConfig({
  testDir: '.',
  testMatch: '**/*.spec.ts',
  fullyParallel: false,
  workers: 1,
  // Los flujos escriben sobre una demo limpia; reintentarlos repetiría mutaciones.
  retries: 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  outputDir: join(raiz, 'test-results'),
  reporter: [['list'], ['html', { outputFolder: join(raiz, 'target/e2e-report'), open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:8080',
    testIdAttribute: 'data-cy',
    locale: 'es-CO',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
