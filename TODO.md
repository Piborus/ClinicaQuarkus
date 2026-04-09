# 📋 QUADRO DE TAREFAS (Estilo Trello)

Este quadro organiza as atividades de melhoria técnica do projeto. Cada seção representa uma **Lista** e cada item um **Card**.

---

## 🟦 LISTA: PARA FAZER (To Do)

### CARD: 🧪 Ampliar Cobertura de Testes
**Descrição:** Implementar testes para garantir a estabilidade do sistema.
**Checklist:**
- [ ] Criar testes unitários para `PacienteServiceImpl` (Caminho feliz e falhas de validação).
- [ ] Criar testes de integração para `TransacaoResource` usando `@QuarkusTest`.
- [ ] Mockar repositórios nos testes de serviço para isolar a lógica.
**Etiqueta:** `Testes` | `Prioridade Média`

---

## 🟨 LISTA: EM ANDAMENTO (Doing)

### CARD: 🏗️ Configuração e Migração para MapStruct
**Descrição:** Substituir o mapeamento manual (setters) por interfaces automatizadas para reduzir código repetitivo.
**Checklist:**
- [ ] Adicionar dependências do MapStruct e Annotation Processor no `pom.xml`.
- [ ] Criar `PacienteMapper`, `TransacaoMapper` e `RelatorioMapper`.
- [ ] Refatorar os Services para injetar e usar os mappers.
- [ ] Remover métodos manuais de conversão (toResponse) das entidades/DTOs.
**Etiqueta:** `Arquitetura` | `Refatoração`

### CARD: 📖 Documentação Avançada com OpenAPI (Swagger)
**Descrição:** Tornar o Swagger útil para desenvolvedores externos.
**Checklist:**
- [ ] Adicionar `@APIResponse` detalhando erros (400, 404, 409, 500) nos Resources.
- [ ] Usar `@Schema` em todos os campos dos DTOs com descrições em português.
- [ ] Definir exemplos realistas para cada endpoint.
**Etiqueta:** `Documentação` | `Dev Experience`

---

## 🟩 LISTA: CONCLUÍDO (Done)

### CARD: 🛡️ Implementação de Bean Validation nos DTOs
**Descrição:** Garantir que todos os dados de entrada sejam validados.
**Checklist:**
- [x] Validar `TransacaoRequest` (@NotNull no valor, @NotBlank na descrição).
- [x] Validar `RelatorioRequest` (@NotBlank no texto, @NotNull no pacienteId).
- [x] Validar `EnderecoRequest` (@NotBlank no logradouro, @Pattern no CEP).
- [x] Garantir o uso de `@Valid` em todos os métodos POST/PUT dos Resources.
**Etiqueta:** `Qualidade de Dados` ✓

### CARD: 🚨 Padronização de Exceções de Negócio
**Descrição:** Refinar o `GlobalExceptionHandler` para usar a hierarquia de `BusinessException`.
**Checklist:**
- [x] Substituir lançamentos de `NotFoundException` (JAX-RS) por `BusinessException` customizada nos serviços.
- [x] Ajustar o `GlobalExceptionHandler` para capturar diferentes tipos de erros de negócio e retornar status HTTP apropriados (400, 404, 409).
- [x] Padronizar o objeto `ErrorResponse` para ser consistente em todos os erros.
- [x] Criar exceções específicas (`NotFoundBusinessException`, `ConflictBusinessException`, etc.) para clareza no código.
**Etiqueta:** `Tratamento de Erro` ✓

### CARD: ⚙️ Centralização Inicial do Exception Mapper
- [x] Criação do `GlobalExceptionHandler`.
- [x] Mapeamento de `ConstraintViolationException` para erros de validação.
- [x] Estrutura base do `ErrorResponse` com timestamp e detalhes.
**Etiqueta:** `Tratamento de Erro` ✓
