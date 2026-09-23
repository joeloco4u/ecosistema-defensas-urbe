const fs = require('fs');
const path = require('path');
const csv = require('csv-parser');
const { Client } = require('pg');

const DATA_DIR = 'C:/Users/Freelance/Documents/urbe db/';
const CHUNK_SIZE = 1000;

const pgConfig = {
  host: 'aws-1-us-east-2.pooler.supabase.com',
  port: 5432,
  user: 'postgres.vuuezzibhsltyfaoscgu',
  password: '3cFEPz2pSrgMQM9',
  database: 'postgres',
  ssl: { rejectUnauthorized: false },
};

const JOBS = [
  {
    file: 'espacio 1.csv',
    table: 'temp_espacio',
    columns: ['id_espacio', 'edificio', 'espacio', 'descripcion', 'capacidad', 'tipo_puesto', 'piso'],
    mapping: {
      id_espacio: 'id_espacio',
      edificio: 'id_edificio',
      espacio: 'espacio',
      descripcion: 'descripcion_espacio',
      capacidad: 'capacidad',
      tipo_puesto: 'tipo_puesto',
      piso: 'piso',
    },
  },
  {
    file: 'seccion 1.csv',
    table: 'temp_seccion',
    columns: ['id_seccion'],
    mapping: { id_seccion: 'id_seccion' },
  },
  {
    file: 'bloque_hora 1.csv',
    table: 'temp_bloque_hora',
    columns: ['id_bloque_hora', 'hora_inicio', 'hora_fin', 'turno'],
    mapping: {
      id_bloque_hora: 'id_bloque_hora',
      hora_inicio: 'hora_inicio',
      hora_fin: 'hora_fin',
      turno: 'turno',
    },
  },
  {
    file: 'persona 1.csv',
    table: 'temp_persona',
    columns: ['id_persona', 'identificac', 'primer_ap', 'segundo_a', 'primer_no', 'segundo_n', 'correo_pri'],
    mapping: {
      id_persona: 'id_persona',
      identificac: 'identificacion',
      primer_ap: 'primer_apellido',
      segundo_a: 'segundo_apellido',
      primer_no: 'primer_nombre',
      segundo_n: 'segundo_nombre',
      correo_pri: 'correo_primario',
    },
  },
  {
    file: 'horario 1.csv',
    table: 'temp_horario',
    columns: ['id_espacio', 'id_seccion', 'id_bloque_', 'id_periodo', 'id_persona', 'fecha_asignado'],
    mapping: {
      id_espacio: 'id_espacio',
      id_seccion: 'id_seccion',
      id_bloque_: 'id_bloque_hora',
      id_periodo: 'id_periodo',
      id_persona: 'id_persona',
      fecha_asignado: 'fecha_asignado',
    },
  },
];

function normalize(row) {
  const out = {};
  for (const [k, v] of Object.entries(row)) {
    if (v == null) {
      out[k] = null;
      continue;
    }
    const s = String(v).trim();
    out[k] = s === '' ? null : s;
  }
  return out;
}

function readCsv(filePath) {
  return new Promise((resolve, reject) => {
    const rows = [];
    fs.createReadStream(filePath)
      .pipe(csv({ skipEmptyLines: true }))
      .on('data', (row) => rows.push(row))
      .on('end', () => resolve(rows))
      .on('error', reject);
  });
}

function buildMultiRowInsert(table, columns, count) {
  const colList = columns.map((c) => `"${c}"`).join(', ');
  const perRow = columns.map((_, i) => `$${i + 1}`).join(', ');
  const valuesSets = [];
  for (let r = 0; r < count; r++) {
    const offset = r * columns.length;
    valuesSets.push(`(${perRow.replace(/\$\d+/g, (m) => `$${Number(m.slice(1)) + offset}`)})`);
  }
  return `INSERT INTO "${table}" (${colList}) VALUES ${valuesSets.join(', ')}`;
}

const client = new Client(pgConfig);

async function processAndUpload(filePath, tableName, columns, mapping, chunkSize) {
  console.log(`Leyendo ${path.basename(filePath)}...`);
  const records = await readCsv(filePath);
  console.log(`  -> ${records.length} filas leídas.`);

  const totalChunks = Math.ceil(records.length / chunkSize);
  let inserted = 0;

  for (let i = 0; i < records.length; i += chunkSize) {
    const chunk = records.slice(i, i + chunkSize);
    const chunkNum = Math.floor(i / chunkSize) + 1;
    console.log(`  Uploading chunk ${chunkNum} de ${totalChunks} a ${tableName}...`);

    const values = [];
    for (const rec of chunk) {
      const norm = normalize(rec);
      values.push(...columns.map((c) => norm[mapping[c]] ?? null));
    }

    const sql = buildMultiRowInsert(tableName, columns, chunk.length);
    await client.query(sql, values);
    inserted += chunk.length;
    console.log(`  -> ${inserted}/${records.length} filas en ${tableName}.`);
  }

  console.log(`✔ ${tableName}: ${inserted} filas insertadas.\n`);
}

async function main() {
  await client.connect();
  console.log('Conectado a Supabase PostgreSQL.\n');

  for (const job of JOBS) {
    const filePath = path.join(DATA_DIR, job.file);
    if (!fs.existsSync(filePath)) {
      console.warn(`⚠ Archivo no encontrado: ${filePath}`);
      continue;
    }
    await processAndUpload(filePath, job.table, job.columns, job.mapping, CHUNK_SIZE);
  }

  await client.end();
  console.log('Migración de staging completada.');
}

main().catch((err) => {
  console.error('FALLO:', err.message);
  process.exit(1);
});