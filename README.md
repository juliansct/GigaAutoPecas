# GigaAutoPeças

Sistema desktop de controle de estoque e vendas para uma loja de autopeças, feito em Java (Swing) com banco de dados MySQL.

## O que o sistema faz

| Módulo | Funções |
|---|---|
| Nova Venda | Caixa com carrinho, busca por nome, categoria ou código de barras, forma de pagamento (dinheiro com troco, Pix, débito, crédito) e baixa automática no estoque |
| Relatório de Vendas | Filtro por período (com calendário), faturamento, nº de vendas, ticket médio, lucro bruto, peças mais vendidas e total por forma de pagamento |
| Peças | Cadastro com categoria e fornecedor, estoque mínimo por peça, reposição de estoque e histórico de entradas e saídas |
| Categorias / Fornecedores | Cadastro, edição e exclusão |
| Funcionários *(só gerente)* | Cadastro com senha provisória e redefinição de senha |
| Minha conta | Cada usuário altera o próprio nome, login e senha |

As senhas ficam no banco como hash SHA-256.

## Requisitos

- MySQL Server 8 e MySQL Workbench
- JDK 21 ou superior (só para rodar pelo código-fonte)
- Eclipse IDE for Java Developers (só para rodar pelo código-fonte)
- Driver JDBC do MySQL: já incluído em `lib/mysql-connector-j-9.7.0.jar`

Para usar só o executável, basta o MySQL Server 8: o Java vai embutido.

## Como rodar

### 1. Criar o banco de dados

1. Abra o MySQL Workbench e conecte na instância local (ex.: *Local instance MySQL80*).
2. File → Open SQL Script… e abra o arquivo `gigapecas_db.sql`.
3. Execute com o botão do raio ou `Ctrl + Shift + Enter`.
4. No painel Schemas, clique com o botão direito e escolha *Refresh All*. O banco `gigapeças_db` deve aparecer com 7 tabelas.

O script cria as tabelas `categoria`, `fornecedor`, `funcionario`, `peca`, `venda`, `item_venda` e `movimentacao_estoque`, e os dados de exemplo: 6 categorias, 4 fornecedores, 18 peças, 4 funcionários e cerca de 45 vendas nos últimos 30 dias.

> Atenção: o script apaga e recria o banco `gigapeças_db`. Se já existir um banco com esse nome, os dados dele serão perdidos.

O MySQL precisa estar ligado sempre que o sistema for usado. Para ele ligar junto com o Windows: tecla Windows → Serviços → MySQL80 → Tipo de inicialização: Automático.

### 2. Criar o config.properties

O sistema lê os dados de acesso ao MySQL do arquivo `config.properties`, que não está no repositório porque contém senha.

1. Faça uma cópia de `config.example.properties` e renomeie para `config.properties`.
2. Preencha com o usuário e a senha do MySQL deste computador:

```properties
db.url=jdbc:mysql://localhost:3306/gigapeças_db
db.user=root
db.password=SUA_SENHA_DO_MYSQL
```

Onde colocar o arquivo:
- executável: ao lado do `GigaAuto.exe`;
- Eclipse: na raiz do projeto, ao lado da pasta `src`.

### 3a. Rodar o executável

O executável já traz o Java embutido. Extraia o `GigaAuto.zip`, coloque o `config.properties` ao lado do `GigaAuto.exe` e dê duplo clique nele. O `.exe` precisa das pastas `app` e `runtime` que estão ao lado dele.

### 3b. Rodar pelo Eclipse

1. File → Import… → General → Existing Projects into Workspace e escolha a pasta do projeto.
2. Se o driver não for reconhecido: botão direito no projeto → Build Path → Configure Build Path → Libraries → Add JARs… → `lib/mysql-connector-j-9.7.0.jar`.
3. Abra `src/gigapecas/view/TelaLogin.java` → Run As → Java Application.

## Usuários e senhas

Ninguém escolhe a própria senha no cadastro. O acesso funciona assim:

1. O gerente cadastra o funcionário em Funcionários. O sistema gera uma senha provisória de 8 caracteres e mostra para o gerente uma única vez.
2. O gerente repassa o usuário e a senha provisória para o funcionário.
3. No primeiro login, o funcionário é obrigado a criar a própria senha, com no mínimo 6 caracteres. A partir daí, nem o gerente sabe qual é.
4. Se alguém esquecer a senha, o gerente usa Redefinir senha, que gera uma nova senha provisória, e o processo recomeça do passo 2.

Cada usuário pode trocar a própria senha quando quiser em Minha conta, na tela de início.

### Primeiro acesso

O único usuário com senha conhecida é o `admin`:

| Usuário | Senha | Cargo |
|---|---|---|
| `admin` | `1234` | Gerente de Estoque |

O `admin` já vem no banco com a senha provisória `1234`, e o sistema pede para trocá-la logo no primeiro acesso.

O banco também traz três funcionários de exemplo, `mariana` (Gerente de Vendas), `carla` (Vendedor) e `joao` (Estoquista), mas sem senha conhecida. Para entrar com um deles:

1. Entre como `admin`.
2. Abra Funcionários, selecione o funcionário e clique em Redefinir senha.
3. Anote a senha provisória que aparecer, saia e entre com o usuário dele e essa senha.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| O sistema não abre ou fecha sem mensagem | MySQL desligado ou `config.properties` ausente | Ligue o MySQL (Serviços → MySQL80 → Iniciar) e confira se o `config.properties` está ao lado do `.exe` |
| `Access denied for user 'root'@'localhost'` | Usuário ou senha errados no `config.properties` | Corrija `db.user` e `db.password` |
| `Unknown database 'gigapeças_db'` | O script do banco não foi executado | Rode o `gigapecas_db.sql` no Workbench (passo 1) |
| `UnsupportedClassVersionError` (pelo Eclipse) | Classes compiladas com um Java mais novo que o usado para rodar | Project → Clean… e rode de novo |

## Estrutura do projeto

```
GigaAuto/
├── src/
│   ├── com/gigapecas/conexao/   Conexão com o banco (lê o config.properties)
│   ├── com/gigapecas/util/      Hash de senha e geração de senha provisória
│   ├── gigapecas/model/         Classes de dados (Peca, Venda, Funcionario...)
│   ├── gigapecas/dao/           Acesso ao banco (comandos SQL)
│   └── gigapecas/view/          Telas do sistema (Swing)
├── lib/
│   └── mysql-connector-j-9.7.0.jar
├── gigapecas_db.sql             Script do banco: estrutura + dados de exemplo
├── config.example.properties    Modelo da configuração (sem senha)
└── README.md
```

## Modelo do banco

```
fornecedor ──(reposição)──► peca ◄── categoria
                              │
                           (venda) ──► venda ──► item_venda
                              │
                 movimentacao_estoque   (histórico de entradas e saídas)

funcionario: quem acessa o sistema, registra vendas e repõe estoque
```

- Peça pertence a uma categoria e a um fornecedor (os dois são opcionais).
- Venda guarda o vendedor, a forma de pagamento e os itens. O nome e o preço de cada item são copiados no momento da venda, para o histórico não mudar se a peça for editada depois.
- Movimentação de estoque registra cada entrada (cadastro e reposição, com o fornecedor) e cada saída (venda, com o número da venda).

