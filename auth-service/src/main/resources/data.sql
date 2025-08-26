INSERT INTO "users" (id, email, password, roles)
SELECT 'b1cc289e-5ada-483c-b54c-50673ada71d0', 'admin@pms.com',
       '$2a$12$/1IIJr./UjWIZacHIAsr5.d8Torxlk8nwOtz4/ARi.YngQiK4ITWe', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id = 'b1cc289e-5ada-483c-b54c-50673ada71d0'
       OR email = 'admin@pms.com'
);

INSERT INTO "users" (id, email, password, roles)
SELECT 'c583b55e-21ea-4291-bf3b-ce1d790a1121', 'qa@pms.com',
       '$2a$12$/1IIJr./UjWIZacHIAsr5.d8Torxlk8nwOtz4/ARi.YngQiK4ITWe', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id = 'c583b55e-21ea-4291-bf3b-ce1d790a1121'
       OR email = 'qa@pms.com'
);
