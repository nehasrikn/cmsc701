from string_generator import StringGenerator
import random
import bbhash
import hashlib
import time
import numpy as np

def hash_string(s, bits):
    """Hashes a string to a bits bit integer.
    """
    hash_value = hash(s) # Convert hexadecimal to decimal
    hashed_int = hash_value % (2**bits)
    return hashed_int

def get_last_n_bits_value(byte_string, n):
    return int.from_bytes(byte_string, "big") & ((1 << n) - 1)

def construct_mphf_and_query(strings, expected_hit_rate, bits_to_use):
    """Samples k strings from strings and partitions them into two sets, 
    one of size k and one of size k_prime.
    """
    random.seed(42)

    keys = random.sample(strings, len(strings))
    
    k_set = keys[:int(len(keys) * expected_hit_rate)]
    k_prime_set = keys[int(len(keys) * expected_hit_rate):]
    
    
    query_set = random.sample(k_set, int(len(k_set) * expected_hit_rate))
    query_set.extend(random.sample(k_prime_set, int(len(k_prime_set) * (1 - expected_hit_rate))))


    uint_hashes = [hash_string(s, 16) for s in k_set]

    num_threads = 1
    gamma = 1.0

    mph = bbhash.PyMPHF(uint_hashes, len(uint_hashes), num_threads, gamma)
    
    fingerprint_array = np.zeros(2**bits_to_use) #[0] * 2**bits_to_use

    for item in k_set:
        hash_value = hashlib.sha512(item.encode()).digest()
        fingerprint_array[get_last_n_bits_value(hash_value, bits_to_use)] = 1
        
    false_positives = 0
    false_positives_fingerprint = 0
    start_time = time.time()

    for item in query_set:
        lookup = mph.lookup(hash_string(item, 16))
        fingerprint = fingerprint_array[get_last_n_bits_value(hashlib.sha512(item.encode()).digest(), bits_to_use)]

        if item not in k_set and lookup is not None:
            false_positives += 1
        if item not in k_set and ((lookup is not None) and (fingerprint == 1)):
            false_positives_fingerprint += 1
    
    end_time = time.time()
    elapsed_time = end_time - start_time

    print("Elapsed Time:", elapsed_time, "seconds")
    print("False positive rate: {}".format(false_positives / len(query_set)))
    print("False positive rate with fingerprint: {}".format(false_positives_fingerprint / len(query_set)))

    return mph


if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)
    #print(len(sg.generate_for_length(31)))
    sample = sg.sample_strings(31, 10000)

    construct_mphf_and_query(sample, 0.8, 16)
        