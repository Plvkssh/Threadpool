#ThreadPool  

## Описание  
Реализация высоконагруженного пула потоков с:  
- Настраиваемыми параметрами (`corePoolSize`, `maxPoolSize`, `keepAliveTime` и т.д.).  
- Логированием событий (создание потока, завершение, отклонение задачи).  
- Балансировкой задач через `Round Robin`.  

## Как запустить  
```bash
cd src/test/java/com/example/threadpool/
javac Main.java && java Main
