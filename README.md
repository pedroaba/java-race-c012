# Java Race

## Membros do Projeto

- **Felipe Silveira Barbosa** - Engenharia de Software
- **Marco Renzo Rodrigues Di Toro** - Engenharia de Software
- **Pedro Augusto Barbosa Aparecido** - Engenharia de Software

## Descrição do Projeto
Java Race é uma simulação de corrida de carros implementada em Java utilizando threads. O projeto demonstra conceitos de programação concorrente, sistema de eventos e implementação de poderes que influenciam a performance durante a corrida.

## Funcionalidades Principais

1. **Sistema de Corrida Multithreading**
   - Cada carro é executado em uma thread separada
   - Os carros movem-se em velocidades diferentes ao longo da pista
   - A corrida termina quando todos os carros cruzam a linha de chegada

2. **Sistema de Eventos**
   - Arquitetura baseada em eventos com Dispatcher e Listeners
   - Eventos para início da corrida, movimento, término e finalização geral
   - Notificações em tempo real do progresso da corrida

3. **Sistema de Poderes**
   - Poderes aleatórios podem ser obtidos durante a corrida
   - Três tipos de poderes disponíveis:
     - **Banana**: Causa uma parada temporária no carro atingido
     - **RedShell**: Reduz a velocidade do carro alvo por um determinado período
     - **Boost**: Aumenta a velocidade do próprio carro por um período de tempo

4. **Carros com Diferentes Características**
   - Ferrari: Velocidade base mais alta (1.3)
   - Lamborghini: Velocidade personalizada
   - Beetle: Velocidade mais baixa, para equilíbrio na corrida

## Tecnologias Utilizadas

- Java 24
- Maven para gerenciamento de dependências
- Arquitetura baseada em eventos
- Programação orientada a objetos
- Programação concorrente com threads

## Arquitetura do Projeto

O projeto é organizado nos seguintes pacotes:

- **entities**: Classes principais como Car e Race
- **events**: Sistema de eventos (Dispatcher, Listener e eventos específicos)
- **powers**: Implementações dos diferentes poderes (Banana, RedShell, Boost)
- **utils**: Classes utilitárias como Sleeper e FormatEpochSecondToString
- **constants**: Configurações e constantes para temporização
- **enums**: Tipos enumerados como GameEventName

## Como Executar

1. Clone o repositório
2. Certifique-se de ter Java 24 instalado
3. Execute o comando: `mvn clean compile exec:java -Dexec.mainClass="pedroaba.java.race.Main"`

## Controles e Parâmetros

- É possível ajustar o comprimento da pista e a quantidade de carros na classe Main
- As constantes de tempo e chance de obtenção de poderes podem ser ajustadas na classe Config

## Funcionamento do Sistema de Poderes

O sistema de poderes funciona da seguinte forma:
1. A cada rodada, o carro tem chance de obter um poder aleatório
2. Os poderes podem ser usados contra adversários ou para benefício próprio
3. Cada poder tem uma duração e efeito específico na velocidade
4. A aplicação de poderes é feita através de um sistema de probabilidade

## Considerações de Design

O projeto foi projetado tendo em mente os seguintes princípios:

1. **Desacoplamento**: Sistema de eventos para comunicação entre componentes
2. **Extensibilidade**: Fácil adição de novos tipos de carros e poderes
3. **Concorrência**: Utilização eficiente de threads para simular a corrida em tempo real
4. **Aleatoriedade**: Elementos de aleatoriedade para gerar corridas únicas e interessantes 