# **TP-3 - BCC362 - 2021-01**

## **Integrantes**
- Carlos Gabriel de Freitas - 19.1.4009
- Carlos Eduardo Romaniello de Souza - 19.1.4003
- Vinicius Gabriel Angelozzi Verona de Resende - 19.1.4005

## **Instruções de execução**
> Compilação do código:
```
make
```

> Execução do código [`Broker`](appl/Broker.java): 
```
make broker
```
**OBS:** É possível a criação de diferentes brokers em múltiplos terminais, de forma a emular um broker primário que possua vários backups, no entanto vale ressaltar que a aplicação atual não possui suporte para a criação de multiplos brokers primários.

> Execução do código [`OneAppl`](appl/OneAppl.java):
```
make client
```

> Deletar os códigos-objeto:
```
make clean
```