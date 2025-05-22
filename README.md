1. Скачиваем JavaFX SDK версии - https://download2.gluonhq.com/openjfx/17.0.15/openjfx-17.0.15_windows-x64_bin-sdk.zip

2. Скачиваем MySQL Server - https://dev.mysql.com/downloads/installer/

3. Скачиваем MySQL Workbench - https://dev.mysql.com/downloads/workbench/

4. Скачиваем Maven - https://maven.apache.org/download.cgi

5. Создаем connection в MySQL Workbench, называем root и задаём пароль root

5. Создаем Schema в MySQL Workbench под названием kursyach

6. Импортируем БД из папки Kursyach_DB, которая находится в корневой папке:  1) Заходим в MySQL Workbench
                                                                             2) Вкладка Server -> Data Import
                                                                             3) Ставим путь к папке Kursyach_DB и жмём кнопку Start Import
7. Запускаем приложение, находясь в корневой папке: mvn javafx:run

