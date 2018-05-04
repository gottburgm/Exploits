### TYPO3 Fingerprinting Script

  #### Arguments

  - **proxy**
  Proxy server to use [Format: scheme://host:port]

  - **debug**
  Debug mode, display each requests

  - **verbose**
  Be more verbose

  - **timeout**
  Max timeout for The HTTP requests

  - **url**
  The target URL [Format: scheme://host]

  - **urls-file**
  The path to the list of urls to test
  
  - **urls-file**
  Try to get version(s) by requesting default files and comparing their checksum
  
  - **cookie**
  Cookie String to use

  - **help**
  Display the help menu

  - **user-agent**
  User-Agent to send to the server

  - **auth**
  Credentials to use for HTTP login [Format: username:password]


  #### Arguments Resume

  ```
            --url [VALUE]        : The target URL [Format: scheme://host]
            --urls-file [FILE]   : The path to the list of urls to test
            --hash-check         : Try to get version(s) by requesting default files and comparing their checksum
            
            --user-agent [VALUE] : User-Agent to send to the server
            --cookie [VALUE]     : Cookie string to use
            --proxy [VALUE]      : Proxy server to use [Format: scheme://host:port]
            --timeout [VALUE]    : Max timeout for The HTTP requests
            --auth [VALUE]       : Credentials to use for HTTP login [Format: username:password]
            --help               : Display this help menu
            --verbose            : Be more verbose
            --debug              : Debug mode, display each requests
  ```
