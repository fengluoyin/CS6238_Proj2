# CS6238_Proj2
CS6238_Proj2

# Steps to run:
# 1. Open a new console, navigate to the Demo/Server folder and execute: 
   java -jar Server.jar
# 2. Navigate a console to the Demo/Client folder and execute: 
   java -jar Client.jar
# 3. Type in the username 'Luoyin1' and the password 'Luoyin1' and input h to get help
    **##############################################################**
    commands and arguments 
    start-session hostname                                   - Start a new session
    check_out DocumentUID                                    - Get file
    check_in DocumentUID SecurityFlag                        - Push file
    delegate DocumentUID Client Time PropagationFlag         - Doing Delegation
    safe_delete DocumentUID                                  - Doing Delegation
    end-session                                              - end the session
    **##############################################################**
# 4. input the following commands:
    `start-session localhost`
    `check_in test1.txt NONE`
    `check_in test2.txt INTEGRITY`
    `check_in test3.txt CONFIDENTIALITY`
    `check_out test1.txt`
    `check_out test2.txt` 
    `check_out test3.txt` 
    `delegate test1.txt Luoyin2 10000 true`
    `delegate test2.txt Luoyin2 10000 true`
    `delegate test3.txt Luoyin2 10000 false`
    `safe_delete test2.txt`
    `end-session`
    `quit`
# 5. Run New Client login in with username 'Luoyin2' and the password 'Luoyin2' and input the following commands:
    `start-session localhost`
    `check_out test1.txt`
    `check_out test2.txt` 
   `check_out test3.txt`
    `delegate test1.txt Luoyin3 10000 true`
    `delegate test3.txt Luoyin3 10000 false`
    `end-session`
    `quit`
# 6. Run New Client login in with username 'Luoyin3' and the password 'Luoyin3' and input the following commands:
    `start-session localhost`
    `check_out test1.txt`
    `check_out test3.txt`
    `end-session`
    `quit`
# 7.Then check the result of the three terminal to check if the function works well

    (1)start-session: check three terminal's first command
    (2)check_in: check the result of the first terminal and the server file_system folder 
    (3)check_out: check the result of the first terminal and the folder and the second terminal and the folder
    (4)delegate: check the second and third terminal to check if the delegation works
    (5)safe_delete: check the second terminal's `check_out test2.txt` result
