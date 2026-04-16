

**Dance Studio Manager**

Documento de Arquitetura e Modelo de Dados

Versão 1.0  |  Março 2026

# **1\. Visão Geral do Projeto**

O Dance Studio Manager é um sistema web de gestão para estúdios de dança. O objetivo é substituir o controle manual via WhatsApp, planilhas e boca a boca por uma plataforma centralizada que gerencie alunos, turmas, presença e cobranças de mensalidades.

| Contexto |  |
| :---- | :---- |
| **Usuários-alvo** | Administradores do estúdio (dona/responsável) |
| **Volume estimado** | 50 a 100 alunos ativos |
| **Problema central** | Controle de pagamentos, faltas e turmas feito manualmente |
| **Objetivo de portfólio** | Demonstrar stack completa Kotlin \+ React \+ PostgreSQL \+ GCP |

# **2\. Stack Tecnológica**

| Tecnologias |  |
| :---- | :---- |
| **Backend** | Kotlin \+ Spring Boot |
| **Frontend** | React (SPA standalone) |
| **Banco de dados** | PostgreSQL (CloudSQL no GCP) |
| **Autenticação** | JWT |
| **Documentação API** | Swagger / OpenAPI |
| **Cloud** | Google Cloud Platform — Cloud Run \+ CloudSQL |
| **Notificações (futuro)** | SendGrid (email) \+ Twilio (WhatsApp) |

# **3\. Arquitetura do Sistema**

## **3.1 Visão geral**

O sistema adota o padrão de monolito modular com frontend desacoplado. O backend expõe uma API REST pura consumida tanto pelo React SPA quanto por um futuro app mobile, sem acoplamento com templates HTML.

![][image1]

## **3.2 Decisões arquiteturais**

**Monolito modular**

O backend é um único projeto Spring Boot organizado internamente por módulos de domínio. Cada módulo possui seu próprio controller, service e repository. A comunicação entre módulos acontece exclusivamente via interfaces de serviço — nunca por acesso direto a repositórios de outro módulo.

Essa abordagem foi escolhida por três razões:

* Complexidade adequada ao porte do projeto (50-100 alunos, dev solo)

* Organização interna limpa e fácil de navegar

* Permite extração futura de módulos sem refatoração do restante

**SPA desacoplado**

O React roda separado do backend como uma Single Page Application. Essa decisão permite que o mesmo backend sirva futuramente um app mobile sem qualquer alteração, já que a API é agnóstica de cliente.

## **3.3 Módulos internos**

| Módulos do backend |  |
| :---- | :---- |
| **students** | Cadastro e gestão de alunos. Dados pessoais, status ativo/inativo. |
| **classes** | Gestão de turmas e modalidades (ballet, jazz, forró etc). |
| **enrollments** | Matrículas — relaciona alunos a planos dentro de turmas. |
| **billing** | Controle de mensalidades. Geração e acompanhamento de pagamentos. |
| **attendance** | Registro de presença por aula. Histórico de faltas por aluno/turma. |
| **notifications** | (Futuro) Envio de alertas por email e WhatsApp via SendGrid e Twilio. |
| **auth** | Autenticação JWT. Controle de roles (ADMIN, futuro STUDENT). |

## **3.4 Estrutura de pastas**

Organização do projeto Kotlin por módulo de domínio:

src/main/kotlin/com/dancestudio/

  students/

    StudentController.kt

    StudentService.kt

    StudentRepository.kt

    Student.kt

  classes/

    ClassController.kt

    ClassService.kt  ...

  enrollments/

  billing/

  attendance/

  auth/

# **4\. Modelo de Dados**

## **4.1 Decisão central: enrollments como pivot**

Um aluno pode estar matriculado em várias turmas simultaneamente (ex: jazz \+ ballet). O valor da mensalidade varia por modalidade e carga horária — não é fixo por aluno.

Por isso, a tabela enrollments é o centro do modelo. Ela conecta um aluno a um plano específico dentro de uma turma. Todos os pagamentos e registros de presença apontam para a matrícula, não diretamente para o aluno.

## **4.2 Fluxo de relacionamentos**

STUDENTS  ──\<  ENROLLMENTS  \>──  PLANS  \>──  CLASSES

                    │

              ┌─────┴──────┐

           PAYMENTS   ATTENDANCE

![][image2]

Exemplo concreto com os dados do estúdio:

* Turma: Jazz (manhã)  →  Plano: Jazz 2h/sem  R$50

* Turma: Ballet (tarde)  →  Plano: Ballet 2h/sem  R$80

* Aluno Maria faz Jazz e Ballet  →  2 enrollments  →  2 pagamentos distintos por mês

## **4.3 Entidades**

**Entidade: students**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único do aluno |
| name | VARCHAR | Nome completo |
| email | VARCHAR | Email de contato |
| phone | VARCHAR | Telefone / WhatsApp |
| birth\_date | DATE | Data de nascimento |
| active | BOOLEAN | Se o aluno está ativo no estúdio |

**Entidade: classes**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único da turma |
| name | VARCHAR | Nome da turma (ex: Jazz Manhã) |
| modality | VARCHAR | Modalidade (ballet, jazz, forró...) |
| schedule | VARCHAR | Horário da aula (ex: Seg/Qua 09:00) |
| max\_students | INTEGER | Limite de vagas na turma |
| active | BOOLEAN | Se a turma está em funcionamento |

**Entidade: plans**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único do plano |
| class\_id | UUID FK | Turma à qual o plano pertence |
| description | VARCHAR | Descrição (ex: Jazz 2h/semana) |
| weekly\_hours | INTEGER | Carga horária semanal |
| monthly\_fee | DECIMAL | Valor mensal cobrado |
| active | BOOLEAN | Se o plano está disponível para matrícula |

**Entidade: enrollments**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único da matrícula |
| student\_id | UUID FK | Aluno matriculado |
| plan\_id | UUID FK | Plano escolhido (turma \+ carga horária \+ valor) |
| enrolled\_at | DATE | Data da matrícula |
| status | ENUM | ACTIVE | INACTIVE | CANCELLED |

**Entidade: payments**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único do pagamento |
| enrollment\_id | UUID FK | Matrícula à qual o pagamento pertence |
| amount | DECIMAL | Valor cobrado |
| due\_date | DATE | Data de vencimento |
| paid\_at | TIMESTAMP | Data/hora do pagamento (null se pendente) |
| status | ENUM | PENDING | PAID | OVERDUE |

**Entidade: attendance**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único do registro |
| enrollment\_id | UUID FK | Matrícula do aluno na turma |
| class\_date | DATE | Data da aula |
| present | BOOLEAN | Se o aluno estava presente |
| notes | TEXT | Observações opcionais (ex: atestado) |

**Entidade: users**

| Campo | Tipo | Descrição |
| :---- | :---- | :---- |
| id | UUID PK | Identificador único do usuário |
| email | VARCHAR | Email para login |
| password\_hash | VARCHAR | Senha criptografada (bcrypt) |
| role | ENUM | ADMIN | STUDENT (futuro) |
| active | BOOLEAN | Se o usuário pode fazer login |

# **5\. Levantamento Tecnológico**

Todas as tecnologias foram escolhidas com dois critérios: alinhamento com a vaga-alvo (Kotlin \+ PostgreSQL \+ GCP) e curva de aprendizado adequada ao contexto de projeto solo com 1-2h/dia.

## **5.1 Backend**

| Tecnologia | Versão | Uso no projeto | Por que essa escolha |
| :---- | :---- | :---- | :---- |
| **Kotlin** | 1.9+ | Linguagem principal | Exigida na vaga. Mais expressiva que Java, null-safety nativa. |
| **Spring Boot** | 3.x | Framework web \+ DI | Padrão de mercado JVM. Ecossistema maduro, boa documentação. |
| **Spring Modulith** | 1.2+ | Monolito modular formal | Impõe limites de módulo em testes. Gera diagrama C4 automaticamente — ideal para portfólio. |
| **Spring Security** | 6.x | Autenticação JWT | Integrado ao Spring Boot, configuração declarativa. |
| **Spring Data JPA** | 3.x | ORM e repositórios | Elimina boilerplate de SQL para operações comuns. |
| **Flyway** | 9.x | Migrations de banco | Versionamento do schema desde o primeiro commit. |
| **Bean Validation** | 3.x | Validação de entrada | Anotações @NotBlank, @Email, @Positive nos DTOs. |
| **Swagger / OpenAPI** | 3.x | Documentação da API | Interface visual dos endpoints — essencial para portfólio. |
| **JUnit 5 \+ MockK** | Latest | Testes unitários | MockK é o padrão para mocks em Kotlin (melhor que Mockito). |
| **Testcontainers** | Latest | Testes de integração | Sobe PostgreSQL real em Docker durante os testes. |

## **5.2 Banco de dados**

| Tecnologia | Versão | Uso no projeto | Por que essa escolha |
| :---- | :---- | :---- | :---- |
| **PostgreSQL** | 15+ | Banco principal | Exigido na vaga. Robusto, suporte a UUID nativo, excelente com JPA. |
| **CloudSQL** | — | PostgreSQL gerenciado na GCP | Free tier, backups automáticos, integração nativa com Cloud Run. |
| **H2 (testes)** | Latest | Banco em memória | Testes unitários rápidos sem dependência de container. |

## **5.3 Frontend**

| Tecnologia | Versão | Uso no projeto | Por que essa escolha |
| :---- | :---- | :---- | :---- |
| **React** | 18+ | Framework UI | Exigido na vaga. Ecossistema rico, componentes reutilizáveis. |
| **Vite** | 5+ | Build tool | Setup mínimo, hot reload rápido, ideal para aprendizado. |
| **React Router** | 6+ | Navegação SPA | Padrão para roteamento em aplicações React. |
| **React Query** | 5+ | Gerenciamento de estado servidor | Simplifica chamadas à API com cache, loading e error states. |
| **Axios** | Latest | Cliente HTTP | Simples e direto para chamadas REST com interceptors JWT. |

## **5.4 Infraestrutura e DevOps**

| Tecnologia | Versão | Uso no projeto | Por que essa escolha |
| :---- | :---- | :---- | :---- |
| **Docker** | 24+ | Containerização | Ambiente idêntico em dev, CI e produção. Build multi-stage. |
| **Docker Compose** | 2+ | Orquestração local | Sobe app \+ PostgreSQL com um único comando em dev. |
| **GCP Cloud Run** | — | Hospedagem backend | Serverless, escala automática, free tier generoso. |
| **GCP CloudSQL** | — | PostgreSQL gerenciado | Backups automáticos, alta disponibilidade, integrado ao Cloud Run. |
| **GitHub Actions** | — | CI/CD | Push → testes → build → deploy automático para o Cloud Run. |
| **GitHub** | — | Controle de versão | Repositório público para o portfólio internacional. |

# **6\. Abordagem de Desenvolvimento — TDD**

A abordagem adotada é TDD (Test-Driven Development) com um ciclo híbrido adaptado para quem está aprendendo a stack ao mesmo tempo que constrói o sistema.

## **6.1 O ciclo TDD**

TDD segue três passos repetidos continuamente para cada funcionalidade:

| Ciclo Red → Green → Refactor |  |
| :---- | :---- |
| **Red** | Escreva um teste que descreve o comportamento esperado. Ele deve falhar — o código ainda não existe. |
| **Green** | Escreva o mínimo de código necessário para o teste passar. Sem otimizações ainda. |
| **Refactor** | Melhore o código sem quebrar os testes. Remova duplicação, melhore nomes, organize. |

## **6.2 Ciclo híbrido adotado neste projeto**

TDD puro desde o primeiro caractere pode ser frustrante quando se está aprendendo a stack ao mesmo tempo. O ciclo abaixo preserva os benefícios do TDD com uma entrada mais suave:

| Passo a passo por módulo |  |
| :---- | :---- |
| **1\. Modele a entidade** | Crie a data class Kotlin e o repository. Entenda o domínio antes de testar. |
| **2\. Escreva os testes do service** | Antes de implementar a lógica, escreva os testes com MockK. Eles vão falhar (Red). |
| **3\. Implemente o service** | Escreva o código até todos os testes passarem (Green). Depois refatore. |
| **4\. Escreva o teste do controller** | Teste o endpoint com @SpringBootTest antes de criar o controller. |
| **5\. Implemente o controller** | Escreva até o teste passar. Confira no Swagger. |

## **6.3 Tipos de teste no projeto**

| Pirâmide de testes |  |
| :---- | :---- |
| **Testes unitários (base)** | Services testados com MockK. Rápidos, sem banco, sem Spring context. Cobrem regras de negócio. |
| **Testes de integração (meio)** | Endpoints testados com @SpringBootTest \+ Testcontainers (PostgreSQL real). Cobrem o fluxo completo. |
| **Testes de módulo (topo)** | Spring Modulith verifica que nenhum módulo viola os limites de outro. Roda como teste JUnit. |

## **6.4 Exemplo prático — módulo billing**

Para fixar o conceito, veja como o ciclo se aplica à regra de negócio mais importante do sistema:

// 1\. RED — escreva o teste antes do código

@Test

fun \`should mark payment as overdue when due date has passed\`() {

    val payment \= Payment(dueDate \= LocalDate.now().minusDays(1), status \= PENDING)

    billingService.processOverduePayments()

    assertEquals(OVERDUE, payment.status)  // falha — método não existe ainda

}

// 2\. GREEN — implemente o mínimo para passar

fun processOverduePayments() {

    paymentRepository.findByStatusAndDueDateBefore(PENDING, LocalDate.now())

        .forEach { it.status \= OVERDUE; paymentRepository.save(it) }

}

// 3\. REFACTOR — melhore sem quebrar o teste

fun processOverduePayments() \=

    paymentRepository.findAllPendingOverdue()

        .map { it.copy(status \= OVERDUE) }

        .let { paymentRepository.saveAll(it) }

| Por que TDD vale o esforço |  |
| :---- | :---- |
| **Segurança para refatorar** | Com testes cobrindo o comportamento, você pode mudar a implementação sem medo. |
| **Documentação viva** | Os testes descrevem o que o sistema faz. Qualquer dev lê e entende as regras de negócio. |
| **Portfólio mais forte** | Cobertura de testes demonstra maturidade de engenharia — raridade entre devs júnior/pleno. |
| **Hábito transferível** | TDD é exigido em muitas empresas americanas. Aprender aqui é investimento direto na vaga. |

# **7\. Plano de Execução**

O plano é organizado em fases, não em datas fixas. Com 1-2 horas por dia, imprevistos são naturais — fase concluída significa avançar, independente de quantos dias levou.

## **Fase 1 — Fundação do projeto  (\~1 semana)**

Estruturar o projeto do zero e ter a primeira rota funcionando. Objetivo: sair do papel.

* Criar projeto Spring Boot com Kotlin via Spring Initializr

* Adicionar Spring Modulith e configurar verificação de módulos

* Configurar Docker Compose local (app \+ PostgreSQL)

* Criar estrutura de pastas por módulo de domínio

* Configurar Flyway para versionamento do schema

* Primeira rota funcional: GET /health

* Configurar Swagger / OpenAPI

* Escrever o teste de verificação de módulos do Spring Modulith

| Entregável — Fase 1 |  |
| :---- | :---- |
| **Resultado** | Projeto rodando via Docker Compose, banco criado, Swagger em /swagger-ui, teste de módulos passando |

## **Fase 2 — Módulos core: students e classes  (\~1 semana)**

Implementar os dois módulos mais simples usando o ciclo TDD híbrido. Esses módulos estabelecem o padrão de código que será repetido nos demais.

* CRUD completo de alunos com validações (Bean Validation) — via TDD

* CRUD de turmas, modalidades e planos — via TDD

* Testes unitários dos services com JUnit 5 \+ MockK

* Testes de integração dos endpoints com Testcontainers

| Entregável — Fase 2 |  |
| :---- | :---- |
| **Resultado** | API de alunos e turmas funcionando com cobertura de testes nos fluxos principais |

## **Fase 3 — Módulos de negócio: enrollments, billing e attendance  (\~2 semanas)**

O coração do sistema — as três dores reais do estúdio implementadas como API, todas desenvolvidas com TDD.

* Módulo enrollments: POST /enrollments, listagem por aluno, cancelamento

* Módulo billing: geração automática de cobranças mensais, atualização de status OVERDUE

* Módulo attendance: registro de chamada por turma/data, relatório de faltas

* Regras de negócio testadas: aluno inadimplente, turma lotada, matrícula duplicada

* Autenticação JWT com Spring Security

| Entregável — Fase 3 |  |
| :---- | :---- |
| **Resultado** | Backend completo e funcional. API documentada no Swagger com autenticação JWT. |

## **Fase 4 — Frontend React  (\~2 semanas)**

Construir as telas que a administradora vai usar no dia a dia. Foco em funcionalidade.

* Setup React \+ Vite \+ React Query

* Tela de login com autenticação JWT

* Dashboard: total de alunos, inadimplentes do mês, vagas por turma

* Tela de alunos: listagem, cadastro e matrícula em turmas

* Tela de chamada: presença por turma e data

* Tela financeira: status de pagamentos do mês

| Entregável — Fase 4 |  |
| :---- | :---- |
| **Resultado** | Sistema usável pela administradora do estúdio. As três dores originais resolvidas em tela. |

## **Fase 5 — Deploy e portfólio  (\~1 semana)**

Colocar o sistema em produção e preparar o projeto para o mercado internacional.

* Dockerfile multi-stage do backend

* Deploy no GCP: Cloud Run \+ CloudSQL

* CI/CD com GitHub Actions: push → testes → build → deploy

* README em inglês: problem statement, stack, architecture, live demo

* Atualizar LinkedIn com link para o repositório e sistema no ar

| Entregável — Fase 5 |  |
| :---- | :---- |
| **Resultado** | Sistema em produção na GCP, GitHub com README em inglês, CI/CD automático rodando. |

| Estimativa total |  |
| :---- | :---- |
| **Tempo estimado** | 7 a 9 semanas com 1-2 horas por dia |
| **Sistema utilizável** | Disponível ao fim da Fase 4 (semana 6-7) |
| **Portfólio completo** | Ao fim da Fase 5 (semana 7-9) |
| **Módulo notificações** | Futuro — após o MVP estar em produção |

[image1]: <data:image/png;base64,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>

[image2]: <data:image/png;base64,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>