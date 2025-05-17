 

int main(void) {
    int a;  
    int result;
    int a1 = 1;  
    {
        int a = 2;  
        int a1 = 2;  
        {
            int a;  
            {
                int a;  
                {
                    int a;  
                    {
                        int a;  
                        {
                            int a; 
                            {
                                int a;  
                                {
                                    int a;  
                                    {
                                        int a;  
                                        {
                                            int a = 20;  
                                            result = a;
                                            {
                                                int a; 
                                                a = 5;
                                                result = result + a;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result = result + a1;
    }
    return result + a1;
}