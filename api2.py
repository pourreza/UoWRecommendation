import re, math
import ast
import json
from collections import Counter

def related(question, ab):
    vector = []
    cosine = []

    vector1 = text_to_vector(question)
    c=0
    vector.append(text_to_vector(ab[c]))
    cosine.append(get_cosine(vector1, vector[c]))
    #     finding maximum cosine value
    d = max(cosine)
    # gettign correspoinding index of that cosine value
    index = cosine.index(d)
    result = cosine[index].str()
    print result[0]
    return result

# defining cosine function
def get_cosine(vec1, vec2):
    intersection = set(vec1.keys()) & set(vec2.keys())

    numerator = sum([vec1[x] * vec2[x] for x in intersection])
    # print ([vec1[x] * vec2[x] for x in intersection])

    sum1 = sum([vec1[x] ** 2 for x in vec1.keys()])
    sum2 = sum([vec2[x] ** 2 for x in vec2.keys()])
    denominator = math.sqrt(sum1) * math.sqrt(sum2)

    if not denominator:
        return 0.0
    else:
        return float(numerator) / denominator
        
# converting texts as vectors
def text_to_vector(text):
    WORD = re.compile(r'\w+')
    words = WORD.findall(text)
    return Counter(words)