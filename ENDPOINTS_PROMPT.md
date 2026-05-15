# Prompt de endpoints (backend -> frontend)

Use este documento como guia para integração do frontend. Todos os paths abaixo são relativos à base do backend. Respostas JSON usam `application/json`, exceto onde indicado.

## Autenticação
- Endpoints protegidos exigem `Authorization: Bearer <accessToken>` e role `ADMINISTRADOR` ou `PSICOLOGO`.
- Fluxo: `POST /auth/login` retorna `accessToken` e `refreshToken`; use o refresh em `POST /auth/refresh`.

## Formatos e enums
- Datas: `dd/MM/yyyy` (ex.: `10/01/1990`)
- Data/hora: `dd/MM/yyyy HH:mm:ss` (ex.: `25/12/2024 14:30:00`)
- Enums:
  - `Sexo`: `MASCULINO | FEMININO`
  - `TipoMovimento`: `ENTRADA | SAIDA`
  - `TipoDePagamento`: `CREDITO | DEBITO | DINHEIRO | PIX`
  - `TipoAntecedenteFamiliar`: `TRANSTORNO_MENTAL | SUICIDIO | ALCOOLISMO | HOMICIDIO | OUTROS`
  - `StatusConsulta`: `AGENDADA | REALIZADA | CANCELADA | NAO_REALIZADA`
  - `StatusConfirmacao`: `PENDENTE | CONFIRMADA | RECUSADA | NAO_RESPONDEU`
  - `TipoUsuario`: `ADMINISTRADOR | PSICOLOGO`
  - `TipoAnamnese`: `INICIAL | REAVALIACAO`

## Respostas de erro (problem+json)
- `content-type: application/problem+json`
- Payload base (ver `dto/response/ErrorResponse.java`):
```json
{
  "type": "<uri>",
  "title": "Bad Request",
  "status": 400,
  "detail": "Erro de validação",
  "instance": "/rota",
  "timestamp": "2024-01-01T10:00:00Z",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    { "field": "campo", "message": "mensagem" }
  ]
}
```

## Endpoints

### Health/hello
**GET `/hello`**
- Descrição: endpoint simples de teste.
- Auth: não.
- Retorno `text/plain`: `Hello from Quarkus REST`.

### Auth
**POST `/auth`**
- Descrição: cadastra usuário.
- Body (`UsuarioRequest`):
```json
{
  "nome": "João",
  "sobrenome": "da Silva",
  "dataNascimento": "15/08/1990",
  "cpf": "123.456.789-00",
  "email": "jda@email.com",
  "senha": "senha123",
  "telefone": "(11) 91234-5678",
  "crp": "11/1112",
  "especialidade": "Psicologia não comportamental"
}
```
- Retorno: `201` com `ApiResponse`.

**POST `/auth/login`**
- Body (`LoginRequest`):
```json
{ "email": "jda@email.com", "senha": "senha123" }
```
- Retorno: `201` com `TokenResponse`.
```json
{ "accessToken": "...", "refreshToken": "..." }
```

**POST `/auth/refresh`**
- Body (`RefreshTokenRequest`):
```json
{ "token": "<refreshToken>" }
```
- Retorno: `201` com novo `TokenResponse`.

**POST `/auth/logout`**
- Body (`RefreshTokenRequest`):
```json
{ "token": "<refreshToken>" }
```
- Retorno: `204` sem body.

**POST `/auth/esqueci-senha`**
- Body (`EsqueciSenhaRequest`):
```json
{ "email": "demo@email.com" }
```
- Retorno: `200` com `ApiResponse`.

**POST `/auth/redefinir-senha`**
- Body (`RedefinirSenhaRequest`):
```json
{ "codigo": "123456", "email": "demo@email.com", "novaSenha": "NovaSenha@123" }
```
- Retorno: `200` com `ApiResponse`.

### Pacientes (auth: ADMINISTRADOR | PSICOLOGO)
**POST `/pacientes`**
- Body (`PacienteRequest`):
```json
{
  "nome": "João da Silva",
  "idade": 30,
  "sexo": "MASCULINO",
  "dataNascimento": "10/01/1990",
  "cpf": "123.456.789-00",
  "rg": "200312312334",
  "telefone": "(11) 91234-5678",
  "email": "jj@gmail.com",
  "endereco": {
    "logradouro": "Av. Paulista",
    "numero": "123",
    "bairro": "Bela Vista",
    "cep": "01311-000",
    "complemento": "Apto 45",
    "cidade": "São Paulo",
    "estado": "SP",
    "pais": "Brasil"
  },
  "responsaveis": [
    {
      "nome": "Ana Clara",
      "idade": 25,
      "cpf": "123.456.789-00",
      "telefone": "(11) 91234-5678",
      "email": "anaclara@email.com",
      "grauDeParentesco": "FILHO"
    }
  ]
}
```
- Retorno: `201` com `ApiResponse`.

**GET `/pacientes/{id}`**
- Retorno: `200` com `PacienteResponse`.

**PUT `/pacientes/{id}`**
- Body: igual ao `PacienteRequest`.
- Retorno: `200` com `ApiResponse`.

**PATCH `/pacientes/delete/{id}`**
- Retorno: `200` com `ApiResponse` (soft delete).

**PATCH `/pacientes/restore/{id}`**
- Retorno: `200` com `ApiResponse`.

**GET `/pacientes`**
- Query: `page` (default 1), `size` (default 10), `sort` (ex.: `nome,asc`), `filterFields`, `filterValues` (listas com mesmo tamanho).
- Retorno: `200` com `PanachePage<PacienteResponse>`.

### Filiações (auth: ADMINISTRADOR | PSICOLOGO)
**GET `/filiacoes/paciente/{id}`**
- Retorno: `200` com `List<FiliacaoResponse>`.

**PUT `/filiacoes/{id}`**
- Body (`FiliacaoRequest`):
```json
{ "nome": "Ana Clara", "idade": 25, "cpf": "123.456.789-00", "telefone": "(11) 91234-5678", "email": "anaclara@email.com", "grauDeParentesco": "FILHO" }
```
- Retorno: `200` com `ApiResponse`.

### Anamnese (auth: ADMINISTRADOR | PSICOLOGO)
**POST `/anamnese`**
- Body (`AnamneseRequest`):
```json
{
  "pacienteId": 1,
  "encaminhamento": "Hospital",
  "historicoAcompanhamento": "Paciente acompanhado por psicólogo",
  "psicodinamicaFamiliar": "Paciente com problemas de autoconhecimento",
  "observacao": "Observações",
  "desenvolvimento": {
    "gravidezParto": "Gravidez tranquila, parto normal",
    "memoriasInfancia": "Infância feliz",
    "memoriasAdolescencia": "Adolescência difícil",
    "faseAdulta": "Fase adulta",
    "faseAtual": "Fase atual",
    "moraComQuem": "Mora com meu pai",
    "numeroFilhos": 2,
    "numeroIrmaos": 3,
    "ordemNascimento": "Primeiro a nascer",
    "fumante": false,
    "etilista": false,
    "usoMedicamento": false,
    "descricaoMedicamentos": "Paracetamol",
    "rotina": "Rotina normal"
  },
  "antecedenteFamiliar": {
    "tiposAntecedentes": ["SUICIDIO"],
    "descricao": "Pai com histórico de depressão"
  }
}
```
- Retorno: `201` com `ApiResponse`.

**PUT `/anamnese/{id}`**
- Body: igual ao `AnamneseRequest`.
- Retorno: `200` com `ApiResponse`.

**GET `/anamnese/{id}`**
- Retorno: `200` com `AnamneseResponse`.

**GET `/anamnese`**
- Query: `page` (default 1), `size` (default 10), `sort`, `filterFields`, `filterValues`.
- Retorno: `200` com `PanachePage<AnamneseResponse>`.

### Consultas (auth: ADMINISTRADOR | PSICOLOGO)
**POST `/consulta`**
- Body (`AgendaRequest`):
```json
{ "idpaciente": 1, "idUsuario": 2, "horario": "25/12/2024 14:30:00" }
```
- Retorno: `201` com `ApiResponse`.

**PATCH `/consulta/cancelar/{id}`**
- Retorno: `200` com `ApiResponse`.

**GET `/consulta/{id}`**
- Retorno: `200` com `ConsultaResponse`.

**GET `/consulta/usuarios/{usuarioId}/horarios-disponiveis`**
- Query: `data` (`dd/MM/yyyy`).
- Retorno: `200` com `List<LocalTime>` (strings em `HH:mm` ou `HH:mm:ss`).

### Carteira (transações) (auth: ADMINISTRADOR | PSICOLOGO)
**POST `/carteira`**
- Body (`CarteiraRequest`):
```json
{ "valor": 150.75, "descricao": "Pagamento de consulta médica", "tipoMovimento": "ENTRADA", "tipoDePagamento": "DINHEIRO", "pacienteId": 1 }
```
- Retorno: `201` com `ApiResponse`.

**GET `/carteira/{id}`**
- Retorno: `200` com `CarteiraResumeResponse`.

**PUT `/carteira/{id}`**
- Body: igual ao `CarteiraRequest`.
- Retorno: `200` com `ApiResponse`.

**GET `/carteira`**
- Query: `page` (default 1), `size` (default 10), `sort`, `filterFields`, `filterValues`.
- Retorno: `200` com `PanachePage<CarteiraResumeResponse>`.

### Prontuário do paciente (auth: ADMINISTRADOR | PSICOLOGO)
**POST `/prontuario-do-paciente`**
- Body (`ProntuarioRequest`):
```json
{ "texto": "Paciente evolui sem intercorrências relevantes desde a última consulta.", "pacienteId": 1 }
```
- Retorno: `201` com `ApiResponse`.

**GET `/prontuario-do-paciente/{id}`**
- Retorno: `200` com `ProntuarioResumeResponse`.

**GET `/prontuario-do-paciente/{id}/paciente`**
- Retorno: `200` com `ProntuarioResponse`.

**PUT `/prontuario-do-paciente/{id}`**
- Body: igual ao `ProntuarioRequest`.
- Retorno: `200` com `ApiResponse`.

**DELETE `/prontuario-do-paciente/{id}`**
- Retorno: `200` com `ApiResponse`.

**GET `/prontuario-do-paciente`**
- Query: `page` (default 1), `size` (default 20), `sort`, `filterFields`, `filterValues`.
- Retorno: `200` com `PanachePage<ProntuarioResponse>`.

### Mailer
**GET `/mailer/send`**
- Descrição: envio de e-mail de teste (destinatário e conteúdo fixos no backend).
- Retorno: `200` com string `"Email enviado com sucesso"`.

**POST `/mailer/lembrete/consultas/disparar`**
- Auth: ADMINISTRADOR | PSICOLOGO.
- Descrição: dispara manualmente a rotina de lembretes.
- Retorno: `200` sem body.

## Estruturas de response (resumo)
- `ApiResponse`: `{ "message": "..." }`
- `TokenResponse`: `{ "accessToken": "...", "refreshToken": "..." }`
- `PanachePage<T>`: `{ "content": [T], "page": { "index": 0, "size": 10 }, "totalCount": 1, "totalPages": 1, "currentPage": 1, "size": 10, "hasNextPage": false, "hasPreviousPage": false }`
- `ConsultaResponse`: `{ id, dataInicio, dataFim, statusConsulta, statusConfirmacao, observacao, paciente: PacienteResumeResponse, usuario: UsuarioResponse }`
- `PacienteResponse`: `{ id, nome, idade, sexo, dataNascimento, cpf, rg, telefone, email, endereco, transacoes, prontuarios, responsaveis }`
- `ProntuarioResponse`: `{ id, texto }`
- `ProntuarioResumeResponse`: `{ id, texto }`
- `CarteiraResumeResponse`: `{ id, valor, descricao, tipoMovimento, tipoDePagamento }`
- `FiliacaoResponse`: `{ id, nome, idade, cpf, telefone, email, grauDeParentesco }`
- `AnamneseResponse`: `{ id, tipoAnamnese, encaminhamento, historicoAcompanhamento, psicodinamicaFamiliar, observacao, paciente, desenvolvimento, antecedenteFamiliar }`

