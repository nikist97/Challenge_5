package InterpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Operator {
	
	private ArrayList<Token> variables;
	private String operatorLine;
	private ArrayList<Method> methods;
	private ArrayList<String> possibleOperators;
	
	public Operator(String operatorline, ArrayList<Token> variables, ArrayList<Method> methods){
		this.variables = variables;
		this.methods = methods;
		this.operatorLine= operatorline;
		possibleOperators = new ArrayList<String>();
		this.possibleOperators.add("=");
        this.possibleOperators.add("+");
        this.possibleOperators.add("-");
		this.possibleOperators.add("*");
		this.possibleOperators.add("/");
		this.possibleOperators.add("%");
	}
	
	 private ArrayList<String> getStringTokens(){
		for(String operator : this.possibleOperators){
            String newOperator = "  " + operator + "  " ;
			this.operatorLine.replace(operator, newOperator);
		}
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(this.operatorLine);
        while(stringTokenizer.hasMoreTokens()){
            tokens.add((stringTokenizer.nextToken()));
        }
        return tokens;
    }
	
	 private Token createToken(String token_input){
        switch (token_input) {

            case "=":
                return new Token("operator", "=");
            case "+":
                return new Token("operator", "+");
            case "-":
                return new Token("operator", "-");
			case "*":
				return new Token("operator", "*");
			case "/":
				return new Token("operator", "/");
			case "%":
				return new Token("operator", "%");

            default:
                if (token_input.matches("^-?\\d+$")){
                    return new Token("integer", Integer.parseInt(token_input));
                }
				else if (token_input.matches("^[A-Z][a-zA-Z]+$")){
					return new Token("method", token_input);
				}
                else if (token_input.matches("^[a-zA-Z]+$")){
                    return new Token("variable", token_input);
                }
                System.out.println(token_input);
                throw new SyntaxException("Wrong syntax with an operator");
        }

    }
	
	private ArrayList<Token> getTokens(){
		ArrayList<String> stringTokens = this.getStringTokens();
		ArrayList<Token> tokens = new ArrayList<Token>();
        for(String stringToken : stringTokens){
			tokens.add(this.createToken(stringToken));
		}
        return tokens;		
	}
	
	private boolean valueExists(Token variable){
        boolean ValueExists = false;
        for(Token testVariable : this.variables){
            if (testVariable.getName().equals(variable.getName())){
                ValueExists = true;
                break;
            }
        }
        return ValueExists;
    }

    private boolean methodExists(String methodName){
		boolean methodExists = false;
		Token method = this.createToken(methodName);
		for(Method testMethod : this.methods){
			if (testMethod.methodName.equals(method.getValue()) && method.getType().equals("method")){
				methodExists = true;
				break;
			}
		}
		return methodExists;
	}
	
	private boolean isValidOperatorStatement(ArrayList<Token> tokens){

		if(tokens.size() >= 3 && tokens.get(2).getType().equals("method")) {
			if(!(tokens.get(0).getType().equals("variable"))){
				return false;
			}
			if (tokens.get(0).getType().equals("variable") && !(valueExists(tokens.get(0)))){
				return false;
			}
			if (!(tokens.get(1).getType().equals("operator") && tokens.get(1).getValue().equals("="))){
				return false;
			}
			if(tokens.get(2).getType().equals("method") && !(methodExists(tokens.get(2).getValue()))){
				return false;
			}
			if (tokens.size() > 3){
				for(int index = 3; index < tokens.size(); index++){
					if(!(tokens.get(index).getType().equals("variable"))){
						return false;
					}
				}
			}

			return true;
		}
		
		if(!(tokens.size() != 3 || tokens.size() != 5)){
			return false;
		}
		
		if(tokens.size() == 3){
			if(!(tokens.get(0).getType().equals("variable"))){
				return false;
			}
            if (tokens.get(0).getType().equals("variable") && !(valueExists(tokens.get(0)))){
                return false;
            }
			if(!(tokens.get(1).getType().equals("operator") && tokens.get(1).getValue().equals("="))){
				return false;
			}
			if(!(tokens.get(2).getType().equals("variable") || tokens.get(2).getType().equals("integer"))){
				return false;
			}
			if (tokens.get(2).getType().equals("variable") && !(valueExists(tokens.get(2)))){
				return false;
			}
		}
		
		else if(tokens.size() == 5){
			if(!(tokens.get(0).getType().equals("variable"))){
				return false;
			}
			if (tokens.get(0).getType().equals("variable") && !(valueExists(tokens.get(0)))){
				return false;
			}
			if(!(tokens.get(1).getType().equals("operator") && tokens.get(1).getValue().equals("="))){
				return false;
			}
			if(!(tokens.get(2).getType().equals("variable") || tokens.get(2).getType().equals("integer"))){
				return false;
			}
			if (tokens.get(2).getType().equals("variable") && !(valueExists(tokens.get(2)))){
				return false;
			}
			if(!(tokens.get(3).getType().equals("operator") && !(tokens.get(3).getValue().equals("=")))){
				return false;
			}
			if(!(tokens.get(4).getType().equals("variable") || tokens.get(4).getType().equals("integer"))){
				return false;
			}
			if (tokens.get(4).getType().equals("variable") && !(valueExists(tokens.get(4)))){
				return false;
			}
		}
		
		return true;
		
	}
	
	public void executeOperator(){
		ArrayList<Token> tokens = this.getTokens();
		
        if (!(isValidOperatorStatement(tokens))){
			throw new SyntaxException("Wrong Syntax or a variable, which was not declared, is used with an operator");
		}

		if(tokens.size() >= 3 && tokens.get(2).getType().equals("method")){
			Token variableToken = tokens.get(0);
			Token methodToken = tokens.get(2);
			Method assignmentMethod = null;
			ArrayList<Token> arguments = new ArrayList<Token>();

			for(Token token : this.variables){
				if(variableToken.getName().equals(token.getName())){
					variableToken = token;
					break;
				}
			}

			for(Method method : this.methods){
				if(methodToken.getValue().equals(method.methodName)){
					assignmentMethod = method;
				}
			}

			if(tokens.size() > 3){
				for(int index = 3; index < tokens.size(); index++){
					Token tokenVar = tokens.get(index);
					for(Token testVar : this.variables){
						if(tokenVar.getName().equals(testVar.getName())){
							arguments.add(testVar);
						}
					}
				}
			}

			int returnNumber = assignmentMethod.executeMethod(arguments);
			variableToken.value = Integer.toString(returnNumber);
		}

		if(tokens.size() == 3 && !tokens.get(2).getType().equals("method")){
			Token variableToken = tokens.get(0);
			Token varIntToken = tokens.get(2);
			
			for(Token token : variables){
				if(variableToken.getName().equals(token.getName())){
					variableToken = token;
					break;
				}
			}
			
			if(varIntToken.getType().equals("variable")){
				for(Token token : variables){
					if(varIntToken.getName().equals(token.getName())){
						varIntToken = token;
						break;
					}
				}
			}
			
			variableToken.value = varIntToken.getValue();
		}
		
		if(tokens.size() == 5 && !tokens.get(2).getType().equals("method")){
			Token variableToken = tokens.get(0);
			Token firstVarIntToken = tokens.get(2);
			Token secondVarIntToken = tokens.get(4);
			Token operator = tokens.get(3);
			
			switch (operator.getValue()) {
				
				case "+":
				
					for(Token token : variables){
						if(variableToken.getName().equals(token.getName())){
							variableToken = token;
							break;
						}
					}
					
                    if(firstVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(firstVarIntToken.getName().equals(token.getName())){
								firstVarIntToken = token;
								break;
							}
						}
					}	

                    if(secondVarIntToken.getType().equals("variable")){
                        for(Token token : variables){
                            if(secondVarIntToken.getName().equals(token.getName())){
                                secondVarIntToken = token;
                                break;
                            }
                        }
					}					

					variableToken.value = Integer.toString(Integer.parseInt(firstVarIntToken.getValue()) + Integer.parseInt(secondVarIntToken.getValue()));

                    break;

                case "-":

                    for(Token token : variables){
                        if(variableToken.getName().equals(token.getName())){
                            variableToken = token;
                            break;
                        }
                    }

                    if(firstVarIntToken.getType().equals("variable")){
                        for(Token token : variables){
                            if(firstVarIntToken.getName().equals(token.getName())){
                                firstVarIntToken = token;
                                break;
                            }
                        }
                    }

                    if(secondVarIntToken.getType().equals("variable")){
                        for(Token token : variables){
                            if(secondVarIntToken.getName().equals(token.getName())){
                                secondVarIntToken = token;
                                break;
                            }
                        }
                    }

                    variableToken.value = Integer.toString(Integer.parseInt(firstVarIntToken.getValue()) - Integer.parseInt(secondVarIntToken.getValue()));

                    break;

				case "*":

					for(Token token : variables){
						if(variableToken.getName().equals(token.getName())){
							variableToken = token;
							break;
						}
					}

					if(firstVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(firstVarIntToken.getName().equals(token.getName())){
								firstVarIntToken = token;
								break;
							}
						}
					}

					if(secondVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(secondVarIntToken.getName().equals(token.getName())){
								secondVarIntToken = token;
								break;
							}
						}
					}

					variableToken.value = Integer.toString(Integer.parseInt(firstVarIntToken.getValue()) * Integer.parseInt(secondVarIntToken.getValue()));

					break;

				case "/":

					for(Token token : variables){
						if(variableToken.getName().equals(token.getName())){
							variableToken = token;
							break;
						}
					}

					if(firstVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(firstVarIntToken.getName().equals(token.getName())){
								firstVarIntToken = token;
								break;
							}
						}
					}

					if(secondVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(secondVarIntToken.getName().equals(token.getName())){
								secondVarIntToken = token;
								break;
							}
						}
					}

					variableToken.value = Integer.toString(Integer.parseInt(firstVarIntToken.getValue()) / Integer.parseInt(secondVarIntToken.getValue()));

					break;

				case "%":

					for(Token token : variables){
						if(variableToken.getName().equals(token.getName())){
							variableToken = token;
							break;
						}
					}

					if(firstVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(firstVarIntToken.getName().equals(token.getName())){
								firstVarIntToken = token;
								break;
							}
						}
					}

					if(secondVarIntToken.getType().equals("variable")){
						for(Token token : variables){
							if(secondVarIntToken.getName().equals(token.getName())){
								secondVarIntToken = token;
								break;
							}
						}
					}

					variableToken.value = Integer.toString(Integer.parseInt(firstVarIntToken.getValue()) % Integer.parseInt(secondVarIntToken.getValue()));

					break;

                default :
                    throw new SyntaxException("Wrong operator syntax");
			}

		}
	}

}
