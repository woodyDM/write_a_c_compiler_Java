 
int foo(int a, int a);

int main(void) {
    return foo(1, 2);
}

int foo(int a, int b) {
    return a + b;
}