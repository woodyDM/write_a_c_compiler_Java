int main(void) {
    int a = 10;
    int b = 12;
    a += 0 || b;  
    b *= a && 0;  

    int c = 14;
    c -= a || b;  

    int d = 16;
    d /= c || d;  
    return (a == 11 && b == 0 && c == 13 && d == 16);
}