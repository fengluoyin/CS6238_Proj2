# CS6238_Proj2
CS6238_Proj2

Steps to run:
1. Open a new console, navigate to the Demo/Server folder and execute: java -jar Server.jar
2. Navigate a console to the Demo/Client folder and execute: java -jar Client.jar
3. Type in the username 'Luoyin1' and the password 'Luoyin1'
4. Copy & paste the below commands  into the client window:
    start-session
    check_in test1.txt NONE
    check_in test2.txt INTEGRITY
    check_in test3.txt CONFIDENTIALITY
    delegate(test1.txt, Luoyin2, 10000, true)
    delegate(test2.txt, Luoyin2, 10000, true)
    delegate(test3.txt, Luoyin2, 10000, false)

