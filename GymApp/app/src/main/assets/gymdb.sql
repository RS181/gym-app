PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS plano (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  nome TEXT NOT NULL,
  data_criacao TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercicio (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  nome TEXT NOT NULL,
  link_video TEXT,
  link_gif TEXT,
  notas TEXT
);

CREATE TABLE IF NOT EXISTS plano_exercicio (
  exercicio_id INTEGER NOT NULL,
  plano_id INTEGER NOT NULL,
  PRIMARY KEY (exercicio_id, plano_id),
  FOREIGN KEY (exercicio_id) REFERENCES exercicio (id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (plano_id) REFERENCES plano (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plano_id ON plano_exercicio(plano_id);

PRAGMA foreign_keys = ON;