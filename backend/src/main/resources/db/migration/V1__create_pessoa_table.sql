CREATE TABLE IF NOT EXISTS pessoa (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    data_nascimento DATE         NOT NULL
);

