# Mutation

## GenerateActivityReportUseCase

| Linha | Mutante | Por que considerei equivalente |
|---:|---|---|
| 193 | `createReport`: remove a chamada de `validateDate` (mutante 1) | As datas já são verificadas antes de `createReport` ser chamado. Tirar essa segunda verificação não muda o resultado. |
| 206 | `getActivityReport`: remove a chamada de `validateDate` (mutante 1) | Esse método só é chamado depois da validação feita antes. Por isso ele não recebe datas inválidas. |
| 240 | `calculateActiveMonths`: remove a chamada de `validateDate` (mutante 1) | `generate` verifica as datas antes de chamar o mtodo. A validação daqui acaba repetindo a mesma checagem. |

## EditActivityClassUseCase

Não encontrei mutantes equivalentes nessa classe. Os mutantes gerados foram mortos pelos testes funcionais e estruturais.
