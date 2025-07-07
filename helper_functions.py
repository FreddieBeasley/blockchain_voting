def dictionary_addition(dict1:dict, dict2:dict):
     new_dict = {}
     new_keys = set(dict1.keys()) | set(dict2.keys())

     for new_key in new_keys:
          new_dict[new_key] = 0

          for dict in [dict1, dict2]:
               if new_key in dict:
                    try:
                         new_dict[new_key] += dict[new_key]
                    except:
                         raise ValueError('dictionaries must contains the same data types')
     
     return new_dict

