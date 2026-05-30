INSERT INTO projects (id, name, description, created_at) VALUES
('11111111-1111-1111-1111-111111111101', 'Portal Clientes', 'Gestión de clientes y accesos', '2026-01-10 09:00:00'),
('11111111-1111-1111-1111-111111111102', 'Facturación', 'Flujo de aprobación de facturas', '2026-01-11 09:00:00'),
('11111111-1111-1111-1111-111111111103', 'Seguimiento', 'Módulo de seguimiento operativo', '2026-01-12 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO redmine_instances (id, name, base_url) VALUES
(1, 'Demo Redmine', 'https://redmine.example.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO requests (id, project_id, title, description, status, priority, demand_type, start_date, end_date, done_ratio, estimated_hours, spent_hours, created_date) VALUES
('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111102', 'Optimización del flujo de aprobación de facturas', 'Reducir pasos manuales en el circuito de aprobación', 'OPEN', 'High', 'Feature', '2026-03-01', '2026-03-31', 10, 40.0, 4.0, '2026-03-04 09:30:00'),
('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101', 'Nueva validación para el formulario de clientes', 'Validaciones de NIF y email en alta de clientes', 'IN_PROGRESS', 'Normal', 'Bug', '2026-03-02', '2026-03-20', 35, 16.0, 6.0, '2026-03-03 17:45:00'),
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111103', 'Ajuste de permisos para el módulo de seguimiento', 'Restringir acceso a roles de auditoría', 'OPEN', 'Low', 'Support', '2026-03-03', '2026-03-15', 0, 8.0, 0.0, '2026-03-03 11:20:00'),
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111101', 'Integración SSO con proveedor corporativo', 'Autenticación federada para usuarios internos', 'OPEN', 'High', 'Feature', '2026-03-05', '2026-04-05', 5, 56.0, 2.0, '2026-03-05 08:15:00'),
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111102', 'Corrección de totales en informe mensual', 'Descuadre en sumatorio de IVA', 'IN_PROGRESS', 'High', 'Bug', '2026-03-06', '2026-03-18', 60, 12.0, 7.0, '2026-03-06 10:00:00'),
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111103', 'Exportación CSV de incidencias abiertas', 'Descarga masiva para análisis externo', 'OPEN', 'Normal', 'Feature', '2026-03-07', '2026-03-28', 20, 24.0, 5.0, '2026-03-07 14:30:00'),
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111101', 'Mejora de rendimiento en listado de clientes', 'Paginación server-side y cache ligera', 'CLOSED', 'Normal', 'Feature', '2026-02-20', '2026-03-01', 100, 32.0, 30.0, '2026-02-21 09:00:00'),
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111102', 'Automatizar recordatorios de vencimiento', 'Notificaciones por email antes del vencimiento', 'OPEN', 'Low', 'Feature', '2026-03-08', '2026-04-01', 0, 20.0, 0.0, '2026-03-08 16:10:00'),
('22222222-2222-2222-2222-222222222209', '11111111-1111-1111-1111-111111111103', 'Panel de métricas de SLA', 'Visualización de cumplimiento por equipo', 'IN_PROGRESS', 'High', 'Feature', '2026-03-09', '2026-04-10', 25, 48.0, 12.0, '2026-03-09 11:45:00'),
('22222222-2222-2222-2222-222222222210', '11111111-1111-1111-1111-111111111101', 'Corregir enlace roto en ficha de cliente', '404 al abrir histórico de contratos', 'OPEN', 'Normal', 'Bug', '2026-03-10', '2026-03-12', 0, 4.0, 0.0, '2026-03-10 08:50:00'),
('22222222-2222-2222-2222-222222222211', '11111111-1111-1111-1111-111111111102', 'Soporte para adjuntos PDF mayores de 5MB', 'Aumentar límite y validar compresión', 'OPEN', 'Normal', 'Support', '2026-03-11', '2026-03-25', 15, 10.0, 1.0, '2026-03-11 13:20:00'),
('22222222-2222-2222-2222-222222222212', '11111111-1111-1111-1111-111111111103', 'Refactor de servicio de notificaciones', 'Unificar colas de email y push', 'IN_PROGRESS', 'High', 'Feature', '2026-03-12', '2026-05-01', 40, 72.0, 28.0, '2026-03-12 09:05:00'),
('22222222-2222-2222-2222-222222222213', '11111111-1111-1111-1111-111111111101', 'Actualizar textos legales del registro', 'Alineación con nueva normativa', 'CLOSED', 'Low', 'Support', '2026-02-15', '2026-02-28', 100, 6.0, 6.0, '2026-02-16 10:30:00'),
('22222222-2222-2222-2222-222222222214', '11111111-1111-1111-1111-111111111102', 'Validación de IBAN en pagos recurrentes', 'Bloquear cuentas inválidas en alta', 'OPEN', 'High', 'Bug', '2026-03-13', '2026-03-22', 0, 14.0, 0.0, '2026-03-13 15:40:00'),
('22222222-2222-2222-2222-222222222215', '11111111-1111-1111-1111-111111111103', 'Migración de datos históricos 2024', 'Importación desde sistema legado', 'IN_PROGRESS', 'High', 'Feature', '2026-03-01', '2026-06-30', 10, 120.0, 15.0, '2026-03-01 07:00:00'),
('22222222-2222-2222-2222-222222222216', '11111111-1111-1111-1111-111111111101', 'Filtro avanzado por segmento de cliente', 'Combinar región, tamaño y sector', 'OPEN', 'Normal', 'Feature', '2026-03-14', '2026-04-14', 0, 28.0, 0.0, '2026-03-14 12:00:00'),
('22222222-2222-2222-2222-222222222217', '11111111-1111-1111-1111-111111111102', 'Error al conciliar pagos parciales', 'Diferencia de céntimos en cierre diario', 'OPEN', 'High', 'Bug', '2026-03-15', '2026-03-19', 5, 18.0, 3.0, '2026-03-15 09:25:00'),
('22222222-2222-2222-2222-222222222218', '11111111-1111-1111-1111-111111111103', 'Documentación API interna de seguimiento', 'OpenAPI y ejemplos de integración', 'OPEN', 'Low', 'Support', '2026-03-16', '2026-04-01', 0, 12.0, 0.0, '2026-03-16 17:00:00'),
('22222222-2222-2222-2222-222222222219', '11111111-1111-1111-1111-111111111101', 'Duplicados en importación masiva de clientes', 'Detectar registros repetidos por email', 'IN_PROGRESS', 'Normal', 'Bug', '2026-03-17', '2026-03-27', 30, 22.0, 8.0, '2026-03-17 08:40:00'),
('22222222-2222-2222-2222-222222222220', '11111111-1111-1111-1111-111111111102', 'Dashboard de aprobaciones pendientes', 'Vista consolidada para responsables', 'OPEN', 'Normal', 'Feature', '2026-03-18', '2026-04-18', 0, 36.0, 0.0, '2026-03-18 11:10:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO redmine_issue_metadata (id, request_id, redmine_instance_id, redmine_id, project_id, project_name, origin_request_code, raw_tracker, raw_status, author_name, assignee_name, redmine_created_date, redmine_updated_date, redmine_closed_date) VALUES
(1, '22222222-2222-2222-2222-222222222201', 1, 2481, 12, 'Facturación', 'REDMINE-2481', 'Feature', 'New', 'Laura P.', 'Daniel R.', '2026-03-04 09:30:00', '2026-03-04 09:30:00', NULL),
(2, '22222222-2222-2222-2222-222222222202', 1, 2473, 11, 'Portal Clientes', 'REDMINE-2473', 'Bug', 'In Progress', 'Carlos M.', 'Ana T.', '2026-03-03 17:45:00', '2026-03-05 10:00:00', NULL),
(3, '22222222-2222-2222-2222-222222222203', 1, 2468, 13, 'Seguimiento', 'REDMINE-2468', 'Support', 'Open', 'Elena S.', NULL, '2026-03-03 11:20:00', '2026-03-03 11:20:00', NULL),
(4, '22222222-2222-2222-2222-222222222204', 1, 2465, 11, 'Portal Clientes', 'REDMINE-2465', 'Feature', 'New', 'Daniel R.', 'Daniel R.', '2026-03-05 08:15:00', '2026-03-05 08:15:00', NULL),
(5, '22222222-2222-2222-2222-222222222205', 1, 2462, 12, 'Facturación', 'REDMINE-2462', 'Bug', 'In Progress', 'Laura P.', 'Carlos M.', '2026-03-06 10:00:00', '2026-03-07 09:00:00', NULL),
(6, '22222222-2222-2222-2222-222222222206', 1, 2459, 13, 'Seguimiento', 'REDMINE-2459', 'Feature', 'New', 'Ana T.', 'Elena S.', '2026-03-07 14:30:00', '2026-03-07 14:30:00', NULL),
(7, '22222222-2222-2222-2222-222222222207', 1, 2455, 11, 'Portal Clientes', 'REDMINE-2455', 'Feature', 'Closed', 'Daniel R.', 'Daniel R.', '2026-02-21 09:00:00', '2026-03-01 18:00:00', '2026-03-01 18:00:00'),
(8, '22222222-2222-2222-2222-222222222208', 1, 2451, 12, 'Facturación', 'REDMINE-2451', 'Feature', 'New', 'Carlos M.', NULL, '2026-03-08 16:10:00', '2026-03-08 16:10:00', NULL),
(9, '22222222-2222-2222-2222-222222222209', 1, 2448, 13, 'Seguimiento', 'REDMINE-2448', 'Feature', 'In Progress', 'Elena S.', 'Laura P.', '2026-03-09 11:45:00', '2026-03-10 08:00:00', NULL),
(10, '22222222-2222-2222-2222-222222222210', 1, 2444, 11, 'Portal Clientes', 'REDMINE-2444', 'Bug', 'New', 'Ana T.', 'Ana T.', '2026-03-10 08:50:00', '2026-03-10 08:50:00', NULL),
(11, '22222222-2222-2222-2222-222222222211', 1, 2440, 12, 'Facturación', 'REDMINE-2440', 'Support', 'New', 'Laura P.', 'Carlos M.', '2026-03-11 13:20:00', '2026-03-11 13:20:00', NULL),
(12, '22222222-2222-2222-2222-222222222212', 1, 2436, 13, 'Seguimiento', 'REDMINE-2436', 'Feature', 'In Progress', 'Daniel R.', 'Daniel R.', '2026-03-12 09:05:00', '2026-03-13 12:00:00', NULL),
(13, '22222222-2222-2222-2222-222222222213', 1, 2432, 11, 'Portal Clientes', 'REDMINE-2432', 'Support', 'Closed', 'Elena S.', 'Elena S.', '2026-02-16 10:30:00', '2026-02-28 16:00:00', '2026-02-28 16:00:00'),
(14, '22222222-2222-2222-2222-222222222214', 1, 2428, 12, 'Facturación', 'REDMINE-2428', 'Bug', 'New', 'Carlos M.', 'Laura P.', '2026-03-13 15:40:00', '2026-03-13 15:40:00', NULL),
(15, '22222222-2222-2222-2222-222222222215', 1, 2424, 13, 'Seguimiento', 'REDMINE-2424', 'Feature', 'In Progress', 'Daniel R.', 'Daniel R.', '2026-03-01 07:00:00', '2026-03-15 09:00:00', NULL),
(16, '22222222-2222-2222-2222-222222222216', 1, 2420, 11, 'Portal Clientes', 'REDMINE-2420', 'Feature', 'New', 'Ana T.', NULL, '2026-03-14 12:00:00', '2026-03-14 12:00:00', NULL),
(17, '22222222-2222-2222-2222-222222222217', 1, 2416, 12, 'Facturación', 'REDMINE-2416', 'Bug', 'New', 'Laura P.', 'Carlos M.', '2026-03-15 09:25:00', '2026-03-15 09:25:00', NULL),
(18, '22222222-2222-2222-2222-222222222218', 1, 2412, 13, 'Seguimiento', 'REDMINE-2412', 'Support', 'New', 'Elena S.', 'Elena S.', '2026-03-16 17:00:00', '2026-03-16 17:00:00', NULL),
(19, '22222222-2222-2222-2222-222222222219', 1, 2408, 11, 'Portal Clientes', 'REDMINE-2408', 'Bug', 'In Progress', 'Daniel R.', 'Ana T.', '2026-03-17 08:40:00', '2026-03-18 10:00:00', NULL),
(20, '22222222-2222-2222-2222-222222222220', 1, 2404, 12, 'Facturación', 'REDMINE-2404', 'Feature', 'New', 'Carlos M.', 'Laura P.', '2026-03-18 11:10:00', '2026-03-18 11:10:00', NULL)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('redmine_instances', 'id'), COALESCE((SELECT MAX(id) FROM redmine_instances), 1));
SELECT setval(pg_get_serial_sequence('redmine_issue_metadata', 'id'), COALESCE((SELECT MAX(id) FROM redmine_issue_metadata), 1));
