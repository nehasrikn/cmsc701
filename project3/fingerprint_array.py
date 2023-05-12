from string_generator import StringGenerator
import random
import bbhash
import hashlib
import time
import os
import numpy as np

def hash_string(s, bits):
    """Hashes a string to a bits bit integer.
    """
    hash_value = hash(s) # Convert hexadecimal to decimal
    hashed_int = hash_value % (2**bits)
    return hashed_int

def get_last_n_bits_value(byte_string, n):
    return int.from_bytes(byte_string, "big") & ((1 << n) - 1)

def construct_mphf_and_query(strings, mixture_rate, bits_to_use):
    """Samples k strings from strings and partitions them into two sets, 
    one of size k and one of size k_prime.
    """
    random.seed(42)
    pivot = len(strings) // 2
    keys = random.sample(strings, len(strings)) # shuffle the strings
    
    k_set = keys[:pivot]
    k_prime_set = keys[pivot:]
    
    offset = int((1 - mixture_rate) * pivot)
    query_set = keys[offset:offset + pivot]

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
    denom = 0

    start_time = time.time()

    for item in query_set:
        lookup = mph.lookup(hash_string(item, 16))
        fingerprint = fingerprint_array[get_last_n_bits_value(hashlib.sha512(item.encode()).digest(), bits_to_use)]

        if item not in k_set and lookup is not None:
            false_positives += 1
        if item not in k_set and ((lookup is not None) and (fingerprint == 1)):
            false_positives_fingerprint += 1
        
        if item not in k_set:
            denom += 1
    
    end_time = time.time()
    elapsed_time = end_time - start_time

    mph.save('temp.mph')

    # print("Elapsed Time:", elapsed_time, "seconds")
    # print("False positive rate: {}".format(false_positives / denom))
    # print("False positive rate with fingerprint: {}".format(false_positives_fingerprint / denom))

    return elapsed_time, false_positives / denom, false_positives_fingerprint / denom, os.path.getsize('temp.mph')


if __name__ == "__main__":
    sg = StringGenerator(min_len=3, max_len=31)
    #print(len(sg.generate_for_length(31)))

    num_bits = [7, 8, 10, 16]
    
    queries = [5000, 10000, 20000]

    mixture = [0.2, 0.4, 0.6, 0.8]

    for b in num_bits:
        print("**** False positive rate: {} ****".format(b))
        for q in queries:
            print("Query size: {}".format(q))
            sample = sg.sample_strings(31, 2*q)

            times = []
            fps = []
            fingerprints = []
            sizes = []

            for m in mixture:
                elapsed_time, observed_fp, observed_fp_finger, size = construct_mphf_and_query(sample, 0.8, b)

                times.append(elapsed_time)
                fps.append(observed_fp)
                fingerprints.append(observed_fp_finger) 
                sizes.append(size)

            print("Times: {}".format(times))
            print("False positives: {}".format(fps))
            print("False Positives finger: {}".format(fingerprints))
            print("Size: {}".format(sizes))

            print()
    
    print()
        