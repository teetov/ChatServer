<p>Серверная часть чата</p>

Настройки запуска можно указать двумя способами: через аргументы командной строки или в properties-файле.

Возможные опции:

<b>Режим доступа.</b> Определяет какие проверки должен пройти клиент, чтобы получить полноценный доступ к серверу.

<ul>
<li><p><b>Свобоный доступ.</b> Вариант по-умолчанию.</p></li>

<li><p><b>Доступ через логин.</b> Чтобы получить доступ клиент должен указать уникальный логин и пароль. Список логинов и пароей хранится в XML формате в папке xml/clients.

Выбор опции через аргумент командной строки: '-login'.

Выбор опции через properties-файл: 'accessType:login'.</p></li>

<li><p><b>Доступ через пароль.</b> Чтобы получить доступ клиент должен указать общий для всего сервера пароль. 

Выбор опции через аргумент командной строки: '-password' или '-password=<em>your_password</em>'. Если конкретный пароль не указан он будет получен из properties-файла.

Выбор опции через properties-файл: 'accessType=password'. Дополнительно необходимо указать значение пароля 'passValue=<em>password</em>'.</p></li>
</ul>

<p><b>Порт.</b> Порт на котором будет размещён сервер. Значение по умолчанию 34543.

Указание своего значение через properties-файл: 'port=<em>your_port</em>'.</p>

<p><b>Максимальное количество подключений.</b> Определяет какое количество клиентов может одноременно быть подключено к серверу. 

Указание своего значение через properties-файл: 'maxConnections=<em>connections_count</em>'.</p>

<br>

Связанные проекты: 
<ul> 
<li><a href="https://github.com/teetov/ChatMessage">ChatMessage</a> - отвечает за обмен сообщениями (необходим для работы чата)</li> 
<li><a href="https://github.com/teetov/ChatClient">ChatClient</a> - клиентская часть</li> 
<ul>