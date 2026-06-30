/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import java.util.Scanner;

/**
 *
 * @author CE181629 - Ngo Manh Quan
 */
public class Validation {

    // Scanner object.
    private final Scanner sc = new Scanner(System.in);

    /**
     * Reads a non-empty string.
     *
     * @param message prompt message
     * @return non-empty string
     */
    public String getNonEmptyString(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    /**
     * Reads a non-empty valid string.
     *
     * @param message prompt message
     * @return valid text
     */
    public String getValidText(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().replaceAll("\\s+", " ").trim();

            if ( //                    && input.matches("^[a-zA-ZÀ-Ỹà-ỹ]+(\\s[a-zA-ZÀ-Ỹà-ỹ]+)*$")) {
                    input.matches("^[a-zA-Z]+(\\s[a-zA-Z]+)*$")) {
                return input;
            }

            System.out.println(message + " must contain only letters (A–Z, a–z) and spaces.");
        }
    }

    /**
     * Reads a non-empty valid string.
     *
     * @param message prompt message
     * @return valid classes
     */
    public String getValidId(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (!input.isEmpty()
                    && input.matches("^[A-Z]{1,}[0-9]{1,}$")) {
                return input;
            }

            System.out.println("   Format: at least 1 uppercase letters followed by at least 1 digits.");
            System.out.println("   Example: E1, C101");
        }
    }

    /**
     * Reads a integer value.
     *
     * @param message prompt message
     * @param min min value
     * @param max max value
     * @return valid integer value
     */
    public int getInt(String message, int min, int max) {
        while (true) {

            if (min > max) {
                System.out.println("No valid options.");
                return -1;
            }

            try {
                String error;

                if (min == max) {
                    error = "Please choise [" + min + "]";
                } else {
                    error = "Please choise [" + min + "-" + max + "]";
                }

                System.out.print(message);
                int n = Integer.parseInt(sc.nextLine());

                if (n >= min && n <= max) {
                    return n;
                }

                System.out.println(error);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    /**
     * Reads a double value.
     *
     * @param message prompt message
     * @param min min value
     * @return valid double value
     */
    public double getDouble(String message, double min) {
        while (true) {
            try {
                System.out.print(message);
                double n = Double.parseDouble(sc.nextLine());

                if (n >= min) {
                    return n;
                }

                System.out.println("Please enter the decimal number greater than " + min + ".");

            } catch (NumberFormatException e) {
                System.out.println("Please enter the decimal number greater than " + min + ".");
            }

        }
    }

    /**
     * Reads a yes/no string.
     *
     * @param message prompt message
     * @return true if yes
     */
    public boolean getYesNo(String message) {

        while (true) {

            System.out.print(message);
            String input = sc.nextLine().trim().toUpperCase();

            if (input.equals("Y") || input.equals("YES")) {
                return true;
            }

            if (input.equals("N") || input.equals("NO")) {
                return false;
            }

            System.out.println("Please enter Y or N.");
        }
    }

    /**
     * Reads a address string.
     *
     * @param message prompt message
     * @return address string
     */
    public String getAddress(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (!input.isEmpty() && input.matches("[a-zA-Z0-9]+([ ,./][a-zA-Z0-9]+)*")) {//123, 456 / 789 / 111
                return input;
            }

            System.out.println("Address contains only letters, numbers, spaces and , . / -");
            System.out.println("Example: 123 Nguyen Van Linh, District 7");
        }
    }

    /**
     * Reads a salary value.
     *
     * @param message prompt message
     * @return salary greater than zero
     */
    public double getSalary(String message) {
        while (true) {
            try {
                System.out.print(message);
                String input = sc.nextLine().trim();

                double salary = Double.parseDouble(input);

                if (salary >= 0) {
                    return salary;
                }

                System.out.println("Salary must be greater than zero");
            } catch (NumberFormatException e) {
                System.out.println("Salary must be greater than zero");
            }
        }
    }

    /**
     * Reads a valid email.
     *
     * @param message prompt message
     * @return email in correct format
     */
    public String getEmail(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (input.matches("^[A-Za-z0-9]+([._-][A-Za-z0-9]+)*@"
                    + "[A-Za-z0-9]+([.-][A-Za-z0-9]+)*"
                    + "\\.[A-Za-z]{2,}$")) {//...@..com
                return input;
            }

            System.out.println("Email must correct format: username@domain.com");
//            System.out.println("   - Username: letters, numbers, ., _, -");
//            System.out.println("   - Domain: letters and numbers");
            System.out.println("Example: example123@gmail.com");
        }
    }
    
    /**
     * Reads a valid phone number.
     *
     * @param message prompt message
     * @return phone number in correct format
     */
    public String checkPhone(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();
            
            if (input.matches("^0[35789]\\d{8}$")) {
                return input;
            }

            System.out.println("Phone number must is number");
        }
    }
}
