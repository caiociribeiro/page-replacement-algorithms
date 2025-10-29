# Simulador de Substituição de Páginas
Este projeto implementa e compara quatro algoritmos de substituição de páginas de memória: FIFO, LRU, Ótimo e Relógio (Segunda Chance).

A simulação utiliza cadeias de referência de página geradas aleatoriamente, permitindo testar a eficiência dos algoritmos em diferentes cenários de carga de trabalho.

## Pré-requisitos
Para compilar e executar este projeto, você precisará ter o seguinte instalado:

* **JDK 23** ou superior.

## Como Compilar
```shell
javac src/*.java
```

## Como Gerar Arquivo de Testes
Antes de rodar a simulação pela primeira vez, gere um arquivo de testes:

```shell
# Gera arquivo pages100000.txt com 100000 inteiros aleatorios de 0 a 20. 
java -cp src FileGenerator 100000 20
```

## Rodando a Simulação
```shell
# Simulacao com 4 frames
java -cp src Main data/pages10000.txt 4
```

