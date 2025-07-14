import random

def remove_used_code(used_code):
     #Get old data
     file = open("students.txt", "r")
     lines = file.readlines()
     file.close()

     #rewrite all data except used_code
     file = open("students.txt", "w")
     for line in lines:
          if not line.startswith(used_code):
               file.write(line)


def get_code():
     #takes inputs
     code_test = input("student code (SMITJO) : ")
     code_key = input("student key (123123) :")    

     file = open("students.txt", "r")

     for line in file:
          code = line[:6]

          if code == code_test.upper():
               key = line[6:].strip()

               if key == code_key:
                    file.close()
                    remove_used_code(code)
                    return code, key

               print("incorrect key for student code")
               file.close()
               return None, None

     print("student code does not exist")
     file.close()
     return None, None




     
print()
