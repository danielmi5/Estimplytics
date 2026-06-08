INSERT INTO components (id, name, category, active, created_at)
SELECT id, name, category, active, created_at
FROM (VALUES
  ('33333333-3333-3333-3333-333333333301'::uuid, 'Negocio', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:00:00'),
  ('33333333-3333-3333-3333-333333333302'::uuid, 'Diseño', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:02:00'),
  ('33333333-3333-3333-3333-333333333303'::uuid, 'UX', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:04:00'),
  ('33333333-3333-3333-3333-333333333304'::uuid, 'Frontend', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:06:00'),
  ('33333333-3333-3333-3333-333333333305'::uuid, 'Backend', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:08:00'),
  ('33333333-3333-3333-3333-333333333306'::uuid, 'APIs', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:10:00'),
  ('33333333-3333-3333-3333-333333333307'::uuid, 'BD', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:12:00'),
  ('33333333-3333-3333-3333-333333333308'::uuid, 'Auth', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:14:00'),
  ('33333333-3333-3333-3333-333333333309'::uuid, 'Seguridad', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:16:00'),
  ('33333333-3333-3333-3333-333333333310'::uuid, 'Analítica', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:18:00'),
  ('33333333-3333-3333-3333-333333333311'::uuid, 'Infraestructura', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:20:00'),
  ('33333333-3333-3333-3333-333333333312'::uuid, 'DevOps', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:22:00'),
  ('33333333-3333-3333-3333-333333333313'::uuid, 'Observabilidad', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:24:00'),
  ('33333333-3333-3333-3333-333333333314'::uuid, 'Rendimiento', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:26:00'),
  ('33333333-3333-3333-3333-333333333315'::uuid, 'QA', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:28:00'),
  ('33333333-3333-3333-3333-333333333316'::uuid, 'Documentación', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:30:00'),
  ('33333333-3333-3333-3333-333333333317'::uuid, 'Configuración', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:32:00'),
  ('33333333-3333-3333-3333-333333333318'::uuid, 'Privacidad', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:34:00'),
  ('33333333-3333-3333-3333-333333333319'::uuid, 'Soporte', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:36:00'),
  ('33333333-3333-3333-3333-333333333320'::uuid, 'Arquitectura', 'Arquitectura', true, TIMESTAMP '2026-01-15 09:38:00')
) AS seed(id, name, category, active, created_at)
WHERE NOT EXISTS (SELECT 1 FROM components LIMIT 1);

-- Usuario demo Daniel (contraseña: Kp9#mX2vQw7nL4)
INSERT INTO users (id, name, email, password, role, created_at)
SELECT
  '44444444-4444-4444-4444-444444444403'::uuid,
  'Daniel',
  'daniel@estimplytics.es',
  '$2a$10$1G8l8pLQmGseNB2tYN3gEe5JpPFrlFVcNjfO1qpRo0m9rl3wvY6ym',
  'ANALYST',
  TIMESTAMP '2026-06-08 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'daniel@estimplytics.es');

INSERT INTO projects (id, name, description, created_at, owner_id)
SELECT
  '11111111-1111-1111-1111-111111111199'::uuid,
  'Proyecto Daniel',
  'Datos de prueba para histórico de estimaciones Auth',
  TIMESTAMP '2026-06-08 10:00:00',
  '44444444-4444-4444-4444-444444444403'::uuid
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE id = '11111111-1111-1111-1111-111111111199'::uuid);

INSERT INTO requests (id, project_id, title, description, status, priority, demand_type, start_date, end_date, done_ratio, estimated_hours, spent_hours, created_date, owner_id, origin_request_code)
SELECT
  '22222222-2222-2222-2222-222222222231'::uuid,
  '11111111-1111-1111-1111-111111111199'::uuid,
  'Histórico Auth — login corporativo',
  'Petición ya analizada y estimada manualmente con componente Auth',
  'OPEN',
  'MEDIUM',
  'Feature',
  DATE '2026-06-01',
  DATE '2026-06-30',
  100,
  24.0,
  24.0,
  TIMESTAMP '2026-06-08 10:05:00',
  '44444444-4444-4444-4444-444444444403'::uuid,
  'Proyecto Daniel-00001'
WHERE NOT EXISTS (SELECT 1 FROM requests WHERE id = '22222222-2222-2222-2222-222222222231'::uuid);

INSERT INTO requests (id, project_id, title, description, status, priority, demand_type, start_date, end_date, done_ratio, estimated_hours, spent_hours, created_date, owner_id, origin_request_code)
SELECT
  '22222222-2222-2222-2222-222222222232'::uuid,
  '11111111-1111-1111-1111-111111111199'::uuid,
  'Prueba Auth — segunda petición',
  'Usar esta petición para probar la sugerencia del histórico con Auth',
  'OPEN',
  'MEDIUM',
  'Feature',
  DATE '2026-06-08',
  DATE '2026-06-30',
  0,
  NULL,
  0.0,
  TIMESTAMP '2026-06-08 10:10:00',
  '44444444-4444-4444-4444-444444444403'::uuid,
  'Proyecto Daniel-00002'
WHERE NOT EXISTS (SELECT 1 FROM requests WHERE id = '22222222-2222-2222-2222-222222222232'::uuid);

INSERT INTO impact_analyses (id, request_id, user_id, version_number, complexity, document_data, updated_at)
SELECT
  '55555555-5555-5555-5555-555555555531'::uuid,
  '22222222-2222-2222-2222-222222222231'::uuid,
  '44444444-4444-4444-4444-444444444403'::uuid,
  1,
  'MEDIUM',
  '{"descripcionAbreviada":"Integración login corporativo","descripcionImpacto":"Afecta al componente Auth","descripcionSolucion":"Adaptar flujo de autenticación existente","requisitosFuncionales":"RF01 login. RF02 logout.","pruebas":"PR01 acceso válido. PR02 acceso denegado."}'::jsonb,
  TIMESTAMP '2026-06-08 10:15:00'
WHERE NOT EXISTS (SELECT 1 FROM impact_analyses WHERE id = '55555555-5555-5555-5555-555555555531'::uuid);

INSERT INTO component_analyses (id, analysis_id, component_id, created_at)
SELECT
  '77777777-7777-7777-7777-777777777731'::uuid,
  '55555555-5555-5555-5555-555555555531'::uuid,
  '33333333-3333-3333-3333-333333333308'::uuid,
  TIMESTAMP '2026-06-08 10:20:00'
WHERE NOT EXISTS (SELECT 1 FROM component_analyses WHERE id = '77777777-7777-7777-7777-777777777731'::uuid);

INSERT INTO estimations (id, analysis_id, version_number, fiability, hours_planning, hours_analysis, hours_development, hours_testing, total_hours, actual_hours_feedback, justification, updated_at)
SELECT
  '66666666-6666-6666-6666-666666666631'::uuid,
  '55555555-5555-5555-5555-555555555531'::uuid,
  1,
  0,
  4,
  4,
  12,
  4,
  24,
  24,
  'Estimación manual validada para alimentar histórico Auth',
  TIMESTAMP '2026-06-08 10:25:00'
WHERE NOT EXISTS (SELECT 1 FROM estimations WHERE id = '66666666-6666-6666-6666-666666666631'::uuid);
