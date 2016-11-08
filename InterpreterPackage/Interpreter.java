package InterpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Interpreter {

    private ArrayList<Token> variables;
    private ArrayList<Method> methods;
    private String sourceCodefile;
    private ArrayList<String> possibleCommands;

    public Interpreter(String sourceCodefile){
        this.sourceCodefile = sourceCodefile;
        this.variables = new ArrayList<Token>();
        this.methods = new ArrayList<Method>();
        this.possibleCommands = new ArrayList<String>();
        this.possibleCommands.add("clear");
        this.possibleCommands.add("incr");
        this.possibleCommands.add("decr");
    }

    private void error(String message){
        throw new SyntaxException(message);
    }

    private ArrayList<String> getStringTokens(String statement){
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(statement);
        while(stringTokenizer.hasMoreTokens()){
            tokens.add((stringTokenizer.nextToken()));
        }
        return tokens;
    }

    private Token createToken(String token_input){
        switch (token_input) {

            case "clear":
                return new Token("command", "clear");
            case "incr":
                return new Token("command", "incr");
            case "decr":
                return new Token("command", "decr");
            case "if":
                return new Token("if");
            case "while":
                return new Token("loop", "while");
            case "is":
                return new Token("is");
            case "not":
                return new Token("not");
            case "do":
                return new Token("do");
            case "end":
                return new Token("end");
            case ":" :
                return  new Token("eol");

            default:
                if (token_input.matches("^-?\\d+$")){
                    return new Token("integer", Integer.parseInt(token_input));
                }
                else if (token_input.matches("^[a-zA-Z]+$")){
                    return new Token("variable", token_input);
                }

                this.error("Wrong syntax");
        }

        return new Token(null);
    }

    private void printVariablesState(){
        for (Token variable : this.variables){
            System.out.print(variable.getName() + " = " + variable.getValue());
            System.out.print("  ");
        }
        System.out.println(" ");
    }

    private String fixStatement(String statement){
        statement = statement.replace(":"," :");
        statement = statement.replace("="," = ");
        statement = statement.replace("+"," + ");
        statement = statement.replace("-"," - ");
        statement = statement.replace("*"," * ");
        statement = statement.replace("/"," / ");
        statement = statement.replace("%"," % ");
        return statement;
    }

    private boolean isMethod(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return stringTokens.size() >= 3 && stringTokens.get(0).equals("define");
    }

    private boolean isStatement(String statement) {
        ArrayList<String> stringTokens = getStringTokens(statement);
        return stringTokens.size() == 2 && this.possibleCommands.contains(stringTokens.get(0));
    }

    private boolean isIfStatement(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("if"));
    }

    private boolean isOperator(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        if (stringTokens.size() != 0){
            Token firstToken = createToken(stringTokens.get(0));
            return firstToken.getType().equals("variable");
        }
        return false;
    }

    private boolean isWhileLoop(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("while"));
    }

    public String readSource(){
        String variablesState = "";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(this.sourceCodefile));
            String line = bufferedReader.readLine();
            while (line != null){

                //cheack for comments
                if (getStringTokens(line).size() != 0 && getStringTokens(line).get(0).startsWith("#")){
                    line = bufferedReader.readLine();
                    continue;
                }

                //check for wrong indentation
                if (line.length() != 0 && line.substring(0,1).equals(" ")){
                    this.error("Wrong Indentation");
                }

                line = fixStatement(line);

                if (this.isMethod(line)){
                    ArrayList<String> methodSource = new ArrayList<String>();
                    methodSource.add(line);
                    while (true){
                        line = bufferedReader.readLine();
                        if (line.length() == 0){
                            continue;
                        }
                        else if (line.length() != 0 && this.getStringTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            methodSource.add(line);
                            break;
                        }
                        else if (line != null && line.substring(0,3).equals("   ")){
                            methodSource.add(line);
                        }

                        else {
                            this.error("Wrong Syntax in method source");
                        }
                    }

                    Method method = new Method(methodSource, this.methods, this.variables);
                    method.createMethod();
                    line = bufferedReader.readLine();
                    continue;
                }

                if (this.isStatement(line)){
                    ArrayList<String> StringTokens = getStringTokens(line);
                    Token currentToken = this.createToken(StringTokens.get(0));
                    Token nextToken = this.createToken(StringTokens.get(1));
                    Statement statement = new Statement(currentToken, nextToken, this.variables);
                    statement.executeCommand();
                    line = bufferedReader.readLine();
                }

                else if (this.isOperator(line)){
                    Operator operator = new Operator(line, this.variables, this.methods);
                    operator.executeOperator();
                    line = bufferedReader.readLine();
                }

                else if (this.isWhileLoop(line)){
                    ArrayList<String> loopSource = new ArrayList<String>();
                    loopSource.add(line);
                    while (true){
                        line = bufferedReader.readLine();
                        if (line.length() == 0){
                            continue;
                        }
                        else if (line.length() != 0 && this.getStringTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                            break;
                        }
                        else if (line != null && line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                        }

                        else {
                            this.error("Wrong Syntax");
                        }
                    }
                    WhileLoop Loop = new WhileLoop(loopSource,this.variables,this.methods);
                    Loop.executeWhileLoop();
                    line = bufferedReader.readLine();
                }

                else if (this.isIfStatement(line)){
                    ArrayList<String> ifSource = new ArrayList<String>();
                    ifSource.add(line);

                    while(true){
                        line = bufferedReader.readLine();

                        if (line.length() == 0){
                            continue;
                        }
                        else if (this.getStringTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            ifSource.add(line);
                            break;
                        }
                        else if (line.substring(0,3).equals("   ")){
                            ifSource.add(line);
                        }
                        else {
                            throw new SyntaxException("Wrong syntax");
                        }
                    }

                    IfStatement ifStatement = new IfStatement(ifSource,this.variables,this.methods);
                    ifStatement.executeIfStatement();
                    line = bufferedReader.readLine();
                }

                else if (line.length() == 0) {
                    line = bufferedReader.readLine();
                }

                else {
                    this.error("Wrong syntax");
                }
            }

            //System.out.println("Final state of variables:");
            for(Token token : this.variables){
                variablesState += token.getName() + " = " + token.getValue() + "  ";
                //System.out.print(token.getName() + " = " + token.getValue());
                //System.out.print("  ");
            }

            return variablesState;

        }

        catch(IOException ioe) {
            System.out.println("IOexception");
            System.out.println("in the main method of interpreter.java specify the absolute or the ralative path of the source code");
        }

        return null;
    }

    public static void main(String[] args) {
        //in the brackets specify the absolute path or relative path of the source code .txt file
        //System.out.println(System.getProperty("user.dir"));
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the relative or absolute path of the source code text file");
        String sourceFile = scanner.next();
        Interpreter interpreter = new Interpreter(sourceFile);
        String variablesFinalState = interpreter.readSource();
        System.out.println("Final state of variables:");
        System.out.println(variablesFinalState);
    }

}
