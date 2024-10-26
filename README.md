![Imagem do WhatsApp de 2024-10-25 à(s) 22 29 09_d368ba0e](https://github.com/user-attachments/assets/4e670723-6a9c-4537-a85b-aac5948e6c90)
![Imagem do WhatsApp de 2024-10-25 à(s) 22 29 08_db4014a2](https://github.com/user-attachments/assets/e37d4686-cf9c-4010-a440-0f3d7115c137)
![Imagem do WhatsApp de 2024-10-25 à(s) 22 29 08_b13e881a](https://github.com/user-attachments/assets/79baaf2f-98f1-4a7e-b5d9-ee703671ed95)
![Imagem do WhatsApp de 2024-10-25 à(s) 22 29 08_d98e4681](https://github.com/user-attachments/assets/01140d42-7597-45e6-b7fa-2eef09cf3a61)
![Imagem do WhatsApp de 2024-10-25 à(s) 22 29 08_8b82f83d](https://github.com/user-attachments/assets/7dfb2554-ba1a-4ca6-9fbf-27d461d9737d)


# MainActivity.java

## Descrição Geral

Este código é a implementação de uma **Activity** Android que serve como tela principal para login e registro de usuários utilizando o **Firebase Authentication** e o **Firebase Realtime Database**. Ele verifica se o usuário já está logado, permite o login e o registro de novos usuários, e também oferece funcionalidade de recuperação de senha.

### Funcionalidades Principais

-   **Login**: Os usuários podem inserir seu e-mail e senha para se autenticar. Se o login for bem-sucedido, o tipo de usuário é verificado no banco de dados. Somente usuários do tipo `Pais` podem acessar o sistema.
    
-   **Registro**: Novos usuários podem se registrar com e-mail e senha. Ao registrar, o sistema verifica se as senhas coincidem, e se o registro for bem-sucedido, o usuário é salvo no **Realtime Database** com o campo `userType` definido como `Pais`.
    
-   **Recuperação de Senha**: A funcionalidade de recuperação de senha permite que os usuários enviem um e-mail para redefinir sua senha.
    

## Estrutura dos Componentes

### Variáveis Principais

1.  **FirebaseAuth e DatabaseReference**:
    
    -   `mAuth`: Instância do **FirebaseAuth** utilizada para autenticar os usuários.
    -   `mDatabase`: Referência para o **Firebase Realtime Database**.
2.  **Componentes da UI**:
    
    -   EditTexts para inserir e-mail e senha de login e registro.
    -   Botões para realizar login, registro, e recuperação de senha.

### Inicialização e Verificação de Usuário

1.  **FirebaseApp**: O **FirebaseApp** é inicializado quando a Activity é criada.
2.  **Verificação de Usuário Logado**: Se o usuário já estiver autenticado, ele é redirecionado diretamente para a `HomeActivity`.

### Login

-   O método `loginUser` é responsável por autenticar o usuário. Se a autenticação for bem-sucedida, o tipo de usuário é verificado no **Realtime Database**. Apenas usuários com o `userType` igual a `Pais` podem acessar o sistema. Caso contrário, o usuário é deslogado e uma mensagem de erro é exibida.

### Registro

-   O método `registerUser` cria uma nova conta para o usuário no Firebase Authentication e salva os dados no **Realtime Database**. O tipo de usuário é definido como `Pais`.

### Recuperação de Senha

-   A funcionalidade de recuperação de senha envia um e-mail para o usuário redefinir sua senha, caso o e-mail esteja registrado.

### Tratamento de Erros

-   O método `handleFirebaseAuthError` lida com diferentes erros que podem ocorrer durante o login ou registro, como e-mail inválido, senha fraca, ou usuário não encontrado.

### Estrutura do Objeto Usuário

-   A classe interna `User` é utilizada para representar um usuário, contendo os campos `uid`, `email`, e `userType`.

## Fluxo da Aplicação

1.  **Tela de Login/Registro**:
    
    -   O usuário pode optar por fazer login ou se registrar.
    -   Se o login for bem-sucedido e o tipo de usuário for `Pais`, o usuário é redirecionado para a `HomeActivity`.
2.  **Registro de Novo Usuário**:
    
    -   O usuário insere seu e-mail, senha e confirma a senha.
    -   Se o registro for bem-sucedido, o usuário é salvo no **Realtime Database** e redirecionado para a `HomeActivity`.
3.  **Recuperação de Senha**:
    
    -   O usuário pode solicitar um e-mail de recuperação de senha, caso tenha esquecido a senha de login.

# HomeActivity.java

## Descrição Geral

Este código implementa a funcionalidade principal da **HomeActivity** de um aplicativo Android que permite aos usuários visualizar, criar, editar e excluir rastreios de alunos. Ele também possui um botão de logout para encerrar a sessão do usuário autenticado. Os dados de rastreios são carregados e armazenados no **Firebase Realtime Database**, enquanto a autenticação é gerenciada pelo **FirebaseAuth**.

### Funcionalidades Principais

-   **Exibição de Rastreamentos**: Carrega e exibe uma lista de rastreamentos (trackings) de alunos a partir do Firebase Realtime Database.
    
-   **CRUD de Rastreamentos**: O usuário pode criar novos rastreamentos, editar ou deletar rastreamentos existentes.
    
-   **Logout**: O botão de logout permite que o usuário encerre a sessão e volte para a tela de login.
    

## Estrutura dos Componentes

### Variáveis Principais

1.  **FirebaseAuth e DatabaseReference**:
    
    -   `mAuth`: Instância do **FirebaseAuth** utilizada para gerenciar a sessão do usuário.
    -   `trackingsRef`: Referência ao **Firebase Realtime Database**, onde os rastreamentos são armazenados.
2.  **Componentes da UI**:
    
    -   **RecyclerView**: Exibe a lista de rastreamentos.
    -   **FloatingActionButton**: Botão para adicionar um novo rastreamento.
    -   **Botão de Logout**: Finaliza a sessão do usuário.
3.  **Adapter e Lista de Rastreamentos**:
    
    -   **TrackingAdapter**: Adapter personalizado que gerencia a exibição dos itens de rastreamento.
    -   **trackingList**: Lista que armazena os rastreamentos carregados do Firebase.

### Carregamento de Rastreamentos

O método `loadTrackings` escuta mudanças no banco de dados Firebase e atualiza a lista de rastreamentos em tempo real. Ele utiliza um **ValueEventListener** para ler os dados e popular a lista `trackingList` com os rastreamentos recuperados.

### Funções CRUD de Rastreamentos

1.  **Criação de Rastreamento**:
    
    -   O **FloatingActionButton** redireciona o usuário para a `CreateActivity`, onde ele pode criar um novo rastreamento.
2.  **Edição de Rastreamento**:
    
    -   Cada item na lista de rastreamentos possui um botão de edição que redireciona para a `CreateActivity` com o ID do rastreamento a ser editado.
3.  **Exclusão de Rastreamento**:
    
    -   Ao clicar no botão de exclusão de um item, um **AlertDialog** é exibido para confirmar a ação de deletar o rastreamento. Se confirmado, o rastreamento é removido do Firebase.

### Logout

O botão de logout chama o método `logout`, que desconecta o usuário atual e o redireciona para a `MainActivity`, finalizando a `HomeActivity`.

## Estrutura do Objeto `Tracking`

A classe interna `Tracking` representa um rastreamento com os seguintes campos:

-   `id`: Identificador do rastreamento.
-   `trackingName`: Nome do rastreamento.
-   `schoolAddress`: Endereço da escola.
-   `homeAddress`: Endereço da casa do aluno.
-   `transportUid`: Identificador do transporte.
-   `studentUid`: Identificador do aluno.

## Fluxo da Aplicação

1.  **Visualização de Rastreamentos**:
    
    -   Quando o usuário acessa a **HomeActivity**, os rastreamentos armazenados no Firebase são carregados e exibidos na **RecyclerView**.
2.  **Criação/Edição de Rastreamento**:
    
    -   O usuário pode clicar no botão de ação flutuante para criar um novo rastreamento, ou clicar no botão de edição em um item para modificar um rastreamento existente.
3.  **Exclusão de Rastreamento**:
    
    -   O usuário pode excluir um rastreamento clicando no botão de exclusão de cada item. Uma confirmação é solicitada antes da exclusão final.
4.  **Logout**:
    
    -   O usuário pode sair da sessão clicando no botão de logout.

# CreateActivity


## Descrição

A classe `CreateActivity` é responsável por gerenciar a criação e atualização de registros de rastreamento de transporte escolar no aplicativo. Esta atividade permite que o usuário insira informações sobre o rastreamento de um estudante, incluindo o nome do rastreamento, endereço da escola, endereço de casa, ID do transporte e ID do aluno. A classe também se conecta ao Firebase para armazenar e recuperar dados de rastreamento.

## Estrutura da Classe

### Atributos

-   **EditText** `trackingNameEditText`: Campo de entrada para o nome do rastreamento.
-   **EditText** `schoolAddressEditText`: Campo de entrada para o endereço da escola.
-   **EditText** `homeAddressEditText`: Campo de entrada para o endereço de casa.
-   **EditText** `transportUidEditText`: Campo de entrada para o ID do transporte.
-   **EditText** `studentUidEditText`: Campo de entrada para o ID do aluno.
-   **Spinner** `transportSpinner`: Componente para selecionar o transporte escolar disponível.
-   **MaterialButton** `registerTrackingButton`: Botão para registrar ou atualizar o rastreamento.
-   **ImageButton** `backButton`: Botão para voltar à tela anterior.
-   **DatabaseReference** `usersRef`: Referência ao banco de dados Firebase para acessar usuários.
-   **List<String>** `transportUserNames`: Lista que armazena os nomes dos transportes escolares.
-   **List<String>** `transportUserShortIds`: Lista que armazena os IDs curtos dos transportes escolares.
-   **ArrayAdapter<String>** `spinnerAdapter`: Adaptador para o spinner de transportes.
-   **FirebaseAuth** `auth`: Instância para autenticação do Firebase.
-   **String** `trackingId`: ID do rastreamento a ser editado, se aplicável.
-   **boolean** `isEditing`: Indica se a atividade está no modo de edição.

### Métodos

#### `onCreate(Bundle savedInstanceState)`

-   **Descrição**: Método chamado quando a atividade é criada. Inicializa componentes de UI, configura referências ao Firebase, carrega dados de rastreamento se em modo de edição e define ouvintes de eventos.
-   **Parâmetros**: `savedInstanceState` - Estado salvo da atividade.

#### `populateTransportSpinner()`

-   **Descrição**: Popula o spinner de transporte escolar com dados do Firebase. Obtém os usuários do tipo "Transporte Escolar" e adiciona seus nomes e IDs ao spinner.

#### `registerTracking()`

-   **Descrição**: Registra um novo rastreamento no Firebase. Verifica se todos os campos estão preenchidos, cria um objeto `Tracking`, gera uma chave única e armazena os dados no banco de dados.

#### `loadTrackingData(String trackingId)`

-   **Descrição**: Carrega os dados de rastreamento existentes do Firebase para os campos de entrada quando o usuário edita um rastreamento. Preenche os campos com as informações do rastreamento.
-   **Parâmetros**: `trackingId` - ID do rastreamento a ser carregado.

#### `updateTracking()`

-   **Descrição**: Atualiza um rastreamento existente no Firebase. Verifica se todos os campos estão preenchidos, cria um novo objeto `Tracking` e substitui os dados existentes no banco de dados.

### Classe Interna: `Tracking`

-   **Descrição**: Classe que representa um objeto de rastreamento. Contém informações sobre o nome do rastreamento, endereço da escola, endereço de casa, ID do transporte e ID do aluno.
-   **Atributos**:
    -   **String** `trackingName`: Nome do rastreamento.
    -   **String** `schoolAddress`: Endereço da escola.
    -   **String** `homeAddress`: Endereço de casa.
    -   **String** `transportUid`: ID do transporte escolar.
    -   **String** `studentUid`: ID do aluno.
-   **Construtores**:
    -   **Tracking(String trackingName, String schoolAddress, String homeAddress, String transportUid, String studentUid)**: Construtor para criar um novo objeto `Tracking` com informações específicas.
    -   **Tracking()**: Construtor padrão necessário para chamadas a `DataSnapshot.getValue(Tracking.class)`.

### Uso

A `CreateActivity` é iniciada quando o usuário deseja registrar ou editar um rastreamento. Dependendo se um `trackingId` é passado na intenção, a atividade pode estar no modo de criação ou de edição. A interação do usuário é gerenciada através de campos de entrada e um spinner, e as operações de registro e atualização são realizadas com base nas entradas do usuário.

# TrackingActivity

## Descrição

A `TrackingActivity` é uma atividade do Android que permite rastrear a localização de um estudante e um transporte em um mapa, utilizando o Google Maps e dados armazenados no Firebase. Os dados de localização são atualizados periodicamente para refletir as mudanças nas posições do estudante e do transporte.

## Dependências

-   Google Maps API
-   Firebase Authentication
-   Firebase Realtime Database
-   Volley para requisições HTTP

## Estrutura da Classe

### Atributos

-   `TextView trackingNameText`: Exibe o nome do rastreamento.
-   `DatabaseReference usersRef`: Referência para a base de dados de usuários no Firebase.
-   `FirebaseAuth mAuth`: Autenticação do Firebase para acessar dados do usuário.
-   `String trackingId`: ID do rastreamento, recebido via Intent.
-   `Handler handler`: Manipulador para agendar atualizações de localização.
-   `Runnable updateLocationRunnable`: Tarefa que atualiza as localizações periodicamente.
-   `MapView mapView`: Componente que exibe o mapa.
-   `GoogleMap googleMap`: Instância do GoogleMap.
-   `LatLng studentLatLng`: Localização do estudante.
-   `LatLng transportLatLng`: Localização do transporte.
-   `LatLng homeLatLng`: Localização da casa.
-   `LatLng schoolLatLng`: Localização da escola.
-   `String homeAddress`: Endereço da casa.
-   `String schoolAddress`: Endereço da escola.

### Constantes

-   `String GEOCODE_URL`: URL da API de geocodificação do Google.
-   `String API_KEY`: Chave de API para acessar os serviços do Google Maps.

### Métodos

#### `onCreate(Bundle savedInstanceState)`

Método chamado ao criar a atividade. Inicializa a interface do usuário, configura o `MapView` e inicia o carregamento dos detalhes de rastreamento. Também configura um `Handler` para atualizar as localizações a cada 5 segundos.

#### `onMapReady(GoogleMap googleMap)`

Método chamado quando o mapa está pronto para ser utilizado. Armazena a instância do GoogleMap e chama o método para atualizar o mapa.

#### `onDestroy()`

Método chamado ao destruir a atividade. Remove callbacks do `Handler` e destrói o `MapView`.

#### `onResume()`, `onPause()`, `onSaveInstanceState(Bundle outState)`

Métodos de ciclo de vida da atividade para gerenciar o estado do `MapView`.

#### `loadTrackingDetails()`

Carrega os detalhes do rastreamento a partir do Firebase usando o ID de rastreamento. Exibe o nome do rastreamento e busca as localizações do estudante e do transporte.

#### `updateLocations()`

Atualiza as localizações do estudante e do transporte a partir do Firebase. Este método é chamado periodicamente pelo `Handler`.

#### `fetchUserLocation(String shortUserId, boolean isStudent)`

Busca a localização de um usuário específico (estudante ou transporte) no Firebase e atualiza as variáveis correspondentes.

#### `geocodeAddress(String address, boolean isHomeAddress)`

Faz uma requisição à API de geocodificação do Google para obter as coordenadas de um endereço. Atualiza as localizações da casa ou escola e chama o método para atualizar o mapa.

#### `updateMap()`

Atualiza o mapa com as localizações atuais do estudante, transporte, casa e escola. Adiciona marcadores personalizados para cada local.

# Tracking

### Descrição

A classe `Tracking` representa os dados de rastreamento de um estudante, incluindo informações sobre o nome do rastreamento, endereços da escola e da casa, e identificadores para o transporte e o estudante. Esta classe é utilizada em conjunto com o Firebase para armazenar e recuperar informações sobre rastreamentos.

### Atributos

-   **id**: `String`
    
    -   Identificador único do rastreamento.
-   **trackingName**: `String`
    
    -   Nome do rastreamento.
-   **schoolAddress**: `String`
    
    -   Endereço da escola do estudante.
-   **homeAddress**: `String`
    
    -   Endereço da casa do estudante.
-   **transportUid**: `String`
    
    -   Identificador do usuário que realiza o transporte.
-   **studentUid**: `String`
    
    -   Identificador do estudante.

### Construtores

-   **Tracking()**
    -   Construtor padrão necessário para chamadas a `DataSnapshot.getValue(Tracking.class)`.
-   **Tracking(String trackingName, String schoolAddress, String homeAddress, String transportUid, String studentUid)**
    -   Construtor que inicializa os atributos da classe.
    -   **Parâmetros**:
        -   `trackingName`: Nome do rastreamento.
        -   `schoolAddress`: Endereço da escola.
        -   `homeAddress`: Endereço da casa.
        -   `transportUid`: Identificador do transporte.
        -   `studentUid`: Identificador do estudante.

### Métodos Getters e Setters

-   **String getId()**
    
    -   Retorna o identificador do rastreamento.
-   **void setId(String id)**
    
    -   Define o identificador do rastreamento.
-   **String getTrackingName()**
    
    -   Retorna o nome do rastreamento.
-   **void setTrackingName(String trackingName)**
    
    -   Define o nome do rastreamento.
-   **String getSchoolAddress()**
    
    -   Retorna o endereço da escola.
-   **void setSchoolAddress(String schoolAddress)**
    
    -   Define o endereço da escola.
-   **String getHomeAddress()**
    
    -   Retorna o endereço da casa.
-   **void setHomeAddress(String homeAddress)**
    
    -   Define o endereço da casa.
-   **String getTransportUid()**
    
    -   Retorna o identificador do transporte.
-   **void setTransportUid(String transportUid)**
    
    -   Define o identificador do transporte.
-   **String getStudentUid()**
    
    -   Retorna o identificador do estudante.
-   **void setStudentUid(String studentUid)**
    
    -   Define o identificador do estudante.
