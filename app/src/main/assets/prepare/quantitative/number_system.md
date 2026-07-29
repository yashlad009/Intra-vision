# Number System
> Module: Quantitative Aptitude
> Difficulty: Beginner to Intermediate
> Estimated Learning Time: 40–50 Minutes
> Prerequisite: Basic Arithmetic

---

# 1. Introduction

Number System is the foundation of the entire Quantitative Aptitude section. Almost every other topic — Percentage, Ratio, Average, Profit and Loss, Time and Work — ultimately relies on properties of numbers.

The Number System deals with classifying numbers, understanding their properties, and applying rules such as divisibility, remainders, factors, and multiples.

At its core, every number belongs to one or more categories such as natural numbers, whole numbers, integers, rational numbers, and irrational numbers.

For example:

- 5 is a Natural Number, Whole Number, Integer, and Rational Number.
- −3 is an Integer and a Rational Number, but not a Natural or Whole Number.
- √2 is an Irrational Number.

Because this topic tests logical and analytical thinking rather than memorization alone, it is considered one of the more challenging yet high-scoring chapters in placement exams.

---

# 2. Why Important

Number System is important because it forms the logical base for numerical reasoning.

A strong grasp of this topic helps you solve:

- Divisibility-based puzzles
- HCF and LCM problems
- Remainder theorem questions
- Base conversion questions
- Factorial and factor-counting problems
- Data Interpretation questions involving number properties

Companies that frequently test this topic include:

- TCS
- Infosys
- Wipro
- Accenture
- Capgemini
- Cognizant
- IBM
- Tech Mahindra

Because Number System questions often require multiple logical steps rather than a direct formula, they are commonly used to test analytical ability in campus placement tests.

---

# 3. Key Concepts

## 3.1 Classification of Numbers

| Type | Description | Example |
|------|-------------|---------|
| Natural Numbers | Counting numbers starting from 1 | 1, 2, 3, ... |
| Whole Numbers | Natural numbers including 0 | 0, 1, 2, 3, ... |
| Integers | Whole numbers and their negatives | ..., −2, −1, 0, 1, 2, ... |
| Rational Numbers | Numbers expressible as p/q (q≠0) | 1/2, 0.75, 5 |
| Irrational Numbers | Numbers not expressible as p/q | √2, π |
| Real Numbers | All rational and irrational numbers | Any number on the number line |
| Prime Numbers | Numbers with exactly 2 factors (1 and itself) | 2, 3, 5, 7, 11 |
| Composite Numbers | Numbers with more than 2 factors | 4, 6, 8, 9 |

## 3.2 Even and Odd Numbers

- Even numbers are divisible by 2 (0, 2, 4, 6, ...)
- Odd numbers are not divisible by 2 (1, 3, 5, 7, ...)

**Rules**

- Even + Even = Even
- Odd + Odd = Even
- Even + Odd = Odd
- Even × Anything = Even
- Odd × Odd = Odd

## 3.3 Divisibility Rules

| Divisor | Rule |
|---------|------|
| 2 | Last digit is even |
| 3 | Sum of digits divisible by 3 |
| 4 | Last two digits divisible by 4 |
| 5 | Last digit is 0 or 5 |
| 6 | Divisible by both 2 and 3 |
| 8 | Last three digits divisible by 8 |
| 9 | Sum of digits divisible by 9 |
| 10 | Last digit is 0 |
| 11 | Difference between sum of digits at odd and even positions is 0 or a multiple of 11 |

## 3.4 HCF and LCM

- **HCF (Highest Common Factor)**: The largest number that divides two or more numbers exactly.
- **LCM (Least Common Multiple)**: The smallest number that is a multiple of two or more numbers.

Relationship: HCF × LCM = Product of the two numbers (for exactly two numbers).

## 3.5 Remainder Theorem Basics

When a number N is divided by D, we get:

N = D × Quotient + Remainder, where 0 ≤ Remainder < D

## 3.6 Factors and Number of Factors

If a number N = a^p × b^q × c^r (prime factorization form), then:

Number of factors = (p+1)(q+1)(r+1)

## 3.7 Factorial Basics

n! (n factorial) = n × (n−1) × (n−2) × ... × 1

Used in finding trailing zeros, permutations, and divisibility problems.

**Number of trailing zeros in n!** = Sum of floor(n/5) + floor(n/25) + floor(n/125) + ...

---

# 4. Formula Sheet

| Concept | Formula |
|---------|---------|
| HCF × LCM | Product of the two numbers |
| Number of factors | (p+1)(q+1)(r+1)... from prime factorization |
| Sum of first n natural numbers | n(n+1)/2 |
| Sum of squares of first n natural numbers | n(n+1)(2n+1)/6 |
| Sum of cubes of first n natural numbers | [n(n+1)/2]² |
| Trailing zeros in n! | floor(n/5)+floor(n/25)+floor(n/125)+... |
| Remainder equation | N = D×Q + R |
| Divisibility by 11 | (Sum of odd position digits) − (Sum of even position digits) = 0 or multiple of 11 |
| nth term check for AP-like number sequences | a + (n−1)d |

---

# 5. Memory Tricks

**Trick 1 — Quick Digit Sum for Divisibility by 3 and 9**

Just add all the digits of the number. If the sum is divisible by 3, the number is divisible by 3. Same logic applies for 9.

**Trick 2 — HCF via LCM Relationship**

If you already know the LCM and the product of two numbers, find the HCF instantly using:

HCF = Product / LCM

**Trick 3 — Trailing Zeros Shortcut**

Only count powers of 5 (not 2), since powers of 2 are always more abundant than powers of 5 in a factorial. Keep dividing n by 5, 25, 125... and add the quotients.

**Trick 4 — Prime Factorization Speeds Up Factor Counting**

Always break the number into prime factors first. Counting factors directly by trial division is slow and error-prone for large numbers.

**Trick 5 — Remainder Pattern Recognition**

For cyclical remainder problems (like powers), find the pattern/cycle length of remainders first, then use modular arithmetic to jump directly to the answer.

---

# 6. Solved Examples

## Example 1

Find the HCF and LCM of 36 and 60.

**Solution**

36 = 2² × 3²

60 = 2² × 3 × 5

HCF = 2² × 3 = 12

LCM = 2² × 3² × 5 = 180

**Answer:** HCF = 12, LCM = 180

---

## Example 2

Find the number of factors of 360.

**Solution**

360 = 2³ × 3² × 5¹

Number of factors = (3+1)(2+1)(1+1) = 4×3×2 = 24

**Answer:** 24 factors

---

## Example 3

Find the number of trailing zeros in 100!

**Solution**

floor(100/5) = 20

floor(100/25) = 4

floor(100/125) = 0

Total = 20+4 = 24

**Answer:** 24 trailing zeros

---

## Example 4

Find the remainder when 2^40 is divided by 7.

**Solution**

Powers of 2 mod 7 cycle every 3 steps: 2, 4, 1, 2, 4, 1, ...

40 mod 3 = 1 (since 39 is divisible by 3)

So the remainder matches the 1st term in the cycle = 2

**Answer:** 2

---

## Example 5

Check whether 913 is divisible by 11.

**Solution**

Digits from right: 3 (odd position), 1 (even position), 9 (odd position)

Sum of odd position digits = 3+9 = 12

Sum of even position digits = 1

Difference = 12−1 = 11, which is a multiple of 11

**Answer:** Yes, 913 is divisible by 11

---

# 7. Advanced Questions

## Advanced Question 1

Find the smallest number which, when divided by 8, 12, and 16, leaves a remainder of 5 in each case.

**Solution**

LCM of 8, 12, 16 = 48

Required number = LCM + Remainder = 48+5 = 53

**Answer:** 53

---

## Advanced Question 2

Find the number of zeros at the end of 50! + 60!

**Solution**

Trailing zeros in 50! = floor(50/5)+floor(50/25) = 10+2 = 12

Trailing zeros in 60! = floor(60/5)+floor(60/25) = 12+2 = 14

Since 60! has more trailing zeros than 50!, when adding, the number of trailing zeros in the sum equals the smaller count, because 50! contributes fewer zeros and its non-zero part determines the final digits.

Trailing zeros in 50!+60! = 12

**Answer:** 12

---

## Advanced Question 3

Find the last two digits of 7^2023.

**Solution**

Powers of 7 follow a cycle of last two digits with period 4:

7¹=07, 7²=49, 7³=43, 7⁴=01, then repeats.

2023 mod 4 = 3

So the last two digits match 7³ = 43

**Answer:** 43

---

## Advanced Question 4

A number when divided by 143 leaves a remainder of 31. Find the remainder when the same number is divided by 13.

**Solution**

143 = 13 × 11

Since 31 = 13×2 + 5, the remainder when the number is divided by 13 must also be consistent with this breakdown.

Remainder when 31 is divided by 13 = 5

Since 143 is a multiple of 13, the remainder when the original number is divided by 13 equals the remainder of 31 divided by 13.

**Answer:** 5

---

# 8. Common Mistakes

❌ Forgetting that HCF × LCM = Product only works for exactly two numbers, not three or more.

❌ Counting trailing zeros by including powers of 2 instead of focusing only on powers of 5.

❌ Applying the divisibility rule for 3 (sum of digits) incorrectly to divisibility by 6 without also checking divisibility by 2.

❌ Confusing prime factorization exponents when calculating the number of factors — remember to add 1 to each exponent before multiplying.

❌ Ignoring the remainder range rule (0 ≤ Remainder < Divisor) and ending up with a negative or oversized remainder.

❌ Assuming all cyclical remainder problems have a cycle length equal to the divisor — the actual cycle length must be checked case by case.

---

# 9. Interview Questions

### Q1. What is the difference between a rational and an irrational number?

A rational number can be expressed as a fraction p/q where q is not zero, while an irrational number cannot be expressed as such a fraction and has a non-terminating, non-repeating decimal expansion.

### Q2. Why is 1 not considered a prime number?

A prime number must have exactly two distinct factors — 1 and itself. Since 1 has only one factor (itself), it does not meet this definition.

### Q3. How do you quickly find the number of factors of a large number?

By expressing the number as a product of its prime factors and then adding 1 to each exponent before multiplying them together.

### Q4. What is the significance of the remainder theorem in number system problems?

It allows any number to be expressed in terms of a divisor, quotient, and remainder, which is the foundation for solving divisibility and cyclicity-based problems.

### Q5. How is HCF used in real-world applications?

HCF is used in problems involving equal distribution, such as cutting ropes into equal pieces or arranging items in equal rows and columns, where the largest common measure is needed.

---

# 10. Quick Revision

✔ Classify numbers correctly: Natural, Whole, Integer, Rational, Irrational, Real

✔ HCF × LCM = Product of two numbers (only for two numbers)

✔ Number of factors from prime factorization: (p+1)(q+1)(r+1)...

✔ Trailing zeros in n! come only from powers of 5

✔ Divisibility by 11 uses the alternating digit sum rule

✔ Cyclicity helps find remainders of large powers quickly

---

# 11. Self Assessment

Before moving to the next chapter, make sure you can:

✅ Classify any given number correctly

✅ Apply all major divisibility rules confidently

✅ Calculate HCF and LCM using prime factorization

✅ Find the number of factors of a given number

✅ Calculate trailing zeros in a factorial

✅ Solve remainder and cyclicity-based problems

If all answers are "Yes," you are ready for the next topic.

---

# 12. Practice Questions

1. Classify the number −7/2: is it rational, irrational, an integer, or a whole number?

2. Find the HCF and LCM of 48 and 180.

3. Find the number of factors of 720.

4. Find the number of trailing zeros in 125!

5. Find the remainder when 3^50 is divided by 7.

6. Check whether 8712 is divisible by 11.

7. Find the smallest number which, when divided by 6, 9, and 15, leaves a remainder of 2 in each case.

8. Find the last two digits of 3^250.

9. If a number leaves a remainder of 17 when divided by 51, find the remainder when the same number is divided by 17.

10. Find the sum of the first 20 natural numbers using the direct formula.

If you can solve these confidently without a calculator, you have built a strong foundation across Percentage, Profit and Loss, Ratio and Proportion, Average, and Number System.

---

# Next Chapter

➡ **Simple Interest and Compound Interest**

Since these chapters build on percentage and number properties covered so far, it is recommended to study them next to continue building your placement-ready aptitude skills.
