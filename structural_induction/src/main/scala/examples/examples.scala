package examples

def tailfac(n: Int): Int = 
    def nfac(k: Int, acc: Int): Int = 
        if k <= 1 then acc else nfac(k-1, k*acc)
    nfac(n, 1)

