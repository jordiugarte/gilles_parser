@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1
define i32 @readInt() #0 {
  %1 = alloca i32, align 4
  %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)
  %3 = load i32, i32* %1, align 4
  ret i32 %3
}

declare i32 @scanf(i8*, ...) #1

@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
define void @println(i32 %x) #0 {
  %1 = alloca i32, align 4
  store i32 %x, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
  ret void
}

declare i32 @printf(i8*, ...) #1

define i32 @main() {
entry:
%x = alloca i32, align 4
%prod1 = add i32 0, 10
%prod2 = add i32 0, 5
%arith1 = add i32 %prod1, %prod2
%arith2 = add i32 0, %arith1
%prod5 = mul i32 3, %prod3 = add i32 0, %arith2
%prod4 = add i32 0, 2
%arith3 = sub i32 %prod3, %prod4
%arith4 = add i32 0, %arith3

%prod6 = sdiv i32 %prod5, 4
%prod7 = add i32 0, %prod6
%prod9 = add i32 0, 5
%arith6 = add i32 0, %prod9
%prod10 = add i32 0, 2
%arith7 = add i32 %prod7, %prod10
%arith8 = add i32 0, %arith7
store i32 %arith8, i32* %x, align 4
%x_val = load i32, i32* %x, align 4 

%y = alloca i32, align 4
%prod11 = add i32 0, %prod8 = add i32 0, 1
%arith5 = add i32 0, %prod8

%arith10 = add i32 0, %prod14
store i32 %arith10, i32* %y, align 4
%y_val = load i32, i32* %y, align 4 

%z = alloca i32, align 4
%prod15 = add i32 0, 5
%arith11 = add i32 0, %prod16
store i32 %arith11, i32* %z, align 4
%z_val = load i32, i32* %z, align 4 

and and %cond1 = icmp eq i32 %prod18 = add i32 0, 1
%arith13 = add i32 0, %prod18
, %prod19 = add i32 0, 4
%arith14 = add i32 0, %prod19


br i1 %cond1, label %if_block1, label %else_block1
if_block1:
call void @println(i32 %x_val)


br label %end1
else_block1:
call void @println(i32 %y_val)


br label %end1
end1:
ret i32 0





ret i32 0
}
