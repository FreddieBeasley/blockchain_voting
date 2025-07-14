import random
import json

class key():
     def __init__(self, exponent, n):
          self.__exponent = exponent
          self.__n = n

     def get_exponent(self):
          return self.__exponent
     
     def get_n(self):
          return self.__n
     
     def get_self(self):
          return (self.get_exponent(),self.get_n())
     
     def __repr__(self):
          return f"{self.get_exponent()}"
     

def generate_keys(self):
     min, max = int(1e100), int(1e200)

     def is_prime(value:int) -> bool:
          if value < 2:
               return False
          
          for i in range(2, int((value**0.5) + 1)):
               if value % i == 0:
                    return False
          
          return True

     def generate_primes(min_value:int, max_value:int) -> int:

          while True:
               value = random.randint(min_value, max_value)

               if is_prime(value):
                    return value

     def gcdExtended(a:int, b:int) -> tuple: 
          # Base Case 
          if a == 0 : 
               return b,0,1
                    
          gcd,x1,y1 = gcdExtended(b%a, a) 
          
          # Update x and y using results of recursive 
          # call 
          x = y1 - (b//a) * x1 
          y = x1 
          
          return gcd,x,y 

     def generate_e(phi_n:int) -> int:
          e = random.randint(3, phi_n - 1)
          while gcdExtended(e, phi_n) != 1:
               e = random.randint(3, phi_n - 1)
          
          return e

     def modulo_inverse(e:int, phi_n:int) -> int:
          
          gcd, x, _ = gcdExtended(e, phi_n)
          if gcd != 1:
               raise ValueError("Modulo Inverse does not exist")
          
          d = x % phi_n

          return d

     prime1 = generate_primes(min, max)
     prime2 = generate_primes(min, max)

     n =  prime1 * prime2
     phi_n = (prime1 - 1) * (prime2 - 1)

     e = generate_e(phi_n)
     d = modulo_inverse(e, phi_n)

     return key(e,n), key(d,n)
     
def encrypt(self, message:str, public_key:key ) -> str:
     e, n = public_key.get_self()

     # m^e mod n = c
     cipher_list = [ (ord(char)**e)%n for char in message] 

     return json.dumps(cipher_list)

def decrypt(self, cipher_json:str, private_key:key) -> str:
     d, n = private_key.get_self()
     
     cipher_text = json.loads(cipher_json)

     # c^d mod n = m
     cipher_list = [(ord(char)**d)%n for char in cipher_text]

     return "".join(item for item in cipher_list)

               
print(int(4e5))


     