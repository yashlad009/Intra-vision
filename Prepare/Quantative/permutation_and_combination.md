# Permutation and Combination

> Module: Quantitative Aptitude
> Difficulty: Beginner to Advanced
> Estimated Learning Time: 75–90 Minutes
> Prerequisite:
> - Basic Multiplication
> - Factorials
> - Probability (Basic)
> - Logical Thinking

---

# 1. Introduction

Permutation and Combination (P&C) is one of the most important topics in Quantitative Aptitude because it teaches us **how to count arrangements and selections without actually listing every possibility.**

Many students think this chapter is difficult because of the formulas.

However,

once you understand the basic idea,

Permutation and Combination becomes one of the easiest and most scoring topics.

This chapter is used in:

- Probability
- Data Science
- Artificial Intelligence
- Machine Learning
- Cryptography
- Scheduling
- Password Generation
- Game Development
- Statistics
- Competitive Programming

Almost every placement company asks at least one question from this chapter.

---

# 2. Why Learn Permutation and Combination?

Suppose you have

10 students

and

you need to choose

3 students

for a project.

Should you write every possible group manually?

No.

Permutation and Combination helps us calculate the answer within seconds.

It is widely used in

- Campus Placements
- Banking Exams
- SSC
- CAT
- GATE
- UPSC CSAT
- Coding Interviews

Companies frequently asking P&C questions include:

- TCS
- Infosys
- Accenture
- Capgemini
- Cognizant
- Deloitte
- IBM
- Wipro
- HCL
- Tech Mahindra

This chapter also forms the foundation of

- Probability
- Counting Techniques
- Graph Theory
- Machine Learning
- Artificial Intelligence

---

# 3. Learning Objectives

After completing this chapter,

you should be able to:

✅ Understand Counting Principles

✅ Calculate Factorials

✅ Solve Permutation Problems

✅ Solve Combination Problems

✅ Identify when Order Matters

✅ Solve Arrangement Questions

✅ Solve Selection Questions

✅ Solve Placement-Level Problems

---

# 4. Basic Terminology

Before learning formulas,

understand these important terms.

---

## 4.1 Counting

Counting means

finding the total number of possible ways

without actually listing every possibility.

Example

If you have

3 shirts

and

2 pants,

how many different dress combinations are possible?

Instead of writing all combinations,

we simply calculate

3 × 2

=

6

This is counting.

---

## 4.2 Arrangement

Arrangement means

placing objects in a specific order.

Example

Letters

A

B

C

Possible Arrangements

ABC

ACB

BAC

BCA

CAB

CBA

Notice

ABC

and

BAC

are different.

Order changes.

Hence,

Arrangement depends upon Order.

---

## 4.3 Selection

Selection means

choosing objects

without caring about order.

Example

Choose

2 students

from

A

B

C

Possible Selections

AB

AC

BC

Notice

AB

and

BA

represent the same pair.

Therefore,

Selection ignores Order.

---

## 4.4 Factorial

Factorial is one of the most important concepts in this chapter.

It is represented by

!

(read as "factorial")

Example

5!

=

5×4×3×2×1

=

120

Factorial is mainly used for

- Permutations
- Combinations
- Probability
- Counting Problems

---

# 5. Fundamental Principle of Counting (FPC)

This is the foundation of the entire chapter.

The Fundamental Principle of Counting states:

> **If one task can be performed in m ways and another independent task can be performed in n ways, then both tasks together can be performed in m × n ways.**

This is also called the

**Multiplication Principle.**

---

## Example 1

A restaurant offers

3 starters

and

4 main courses.

How many meals can be formed?

Solution

Starter Choices

3

Main Course Choices

4

Total Meals

=

3×4

=

12

Answer

12

---

## Example 2

You have

5 shirts

3 pants

2 shoes

Total Dress Combinations

=

5×3×2

=

30

Answer

30

---

## Example 3

A password contains

2 letters

followed by

3 digits.

Letters

26 choices each

Digits

10 choices each

Total Passwords

=

26×26×10×10×10

=

676,000

Answer

676,000

---

## Memory Trick

Whenever you hear

"AND"

think

Multiply.

Example

Shirt

AND

Pant

AND

Shoes

↓

Multiply

---

# 6. What is Factorial?

Factorial means

multiplying a number by every positive integer below it.

---

## Formula

n!

=

n×(n−1)×(n−2)...×2×1

---

## Examples

1!

=

1

---

2!

=

2×1

=

2

---

3!

=

3×2×1

=

6

---

4!

=

4×3×2×1

=

24

---

5!

=

5×4×3×2×1

=

120

---

6!

=

720

---

7!

=

5040

---

8!

=

40320

---

9!

=

362880

---

10!

=

3628800

---

## Important Observation

Factorials grow very rapidly.

This is why large counting problems become manageable using formulas.

---

# 7. Properties of Factorial

These properties are frequently used in aptitude exams.

---

## Property 1

0!

=

1

This is a mathematical definition.

Always remember it.

---

## Property 2

1!

=

1

---

## Property 3

n!

=

n×(n−1)!

Example

6!

=

6×5!

=

6×120

=

720

---

## Property 4

Cancel Factorials whenever possible.

Example

6!

÷5!

=

6

Because

6!

=

6×5!

---

## Property 5

8!

÷6!

=

8×7

=

56

---

## Memory Trick

If the denominator contains a smaller factorial,

expand only until cancellation becomes possible.

Never expand the entire factorial unnecessarily.

---

# 8. What is Permutation?

Permutation means

**Arrangement where Order Matters.**

Whenever the position of objects changes,

it creates a new Permutation.

---

## Real-Life Example

Suppose

A

B

and

C

are standing in a line.

ABC

BAC

CAB

These are all different arrangements.

Hence,

these are different permutations.

---

## Important Rule

If

Order Matters

↓

Permutation

---

## Example

Arrange

A

B

C

Total Arrangements

ABC

ACB

BAC

BCA

CAB

CBA

Total

6

---

# 9. What is Combination?

Combination means

**Selection where Order Does Not Matter.**

Only the selected objects matter,

not their positions.

---

## Example

Choose

2 students

from

A

B

C

Possible Groups

AB

AC

BC

Notice

AB

and

BA

are the same group.

Therefore,

only one selection is counted.

---

## Important Rule

If

Order Does Not Matter

↓

Combination

---

# 10. Difference Between Permutation and Combination

| Permutation | Combination |
|-------------|-------------|
| Arrangement | Selection |
| Order Matters | Order Does Not Matter |
| Position is Important | Position is Not Important |
| Usually Larger Value | Usually Smaller Value |
| Used in Seating Problems | Used in Team Selection |

---

## Easy Way to Remember

Think of

A Race

Players finishing

1st

2nd

3rd

Order matters.

Permutation.

---

Think of

Selecting

3 students

for a committee.

Only selection matters.

Combination.

---

# 11. Formula Sheet

These formulas should be memorized.

---

## Formula 1

Factorial

n!

=

n×(n−1)...×1

---

## Formula 2

0!

=

1

---

## Formula 3

Fundamental Principle

Total Ways

=

Multiply the choices

---

### Note

Permutation and Combination formulas

will be introduced in **Part 2**

after understanding the concepts.

---

# 12. Memory Tricks

### Trick 1

Arrangement

↓

Permutation

---

### Trick 2

Selection

↓

Combination

---

### Trick 3

Order Matters

↓

Permutation

---

### Trick 4

Order Doesn't Matter

↓

Combination

---

### Trick 5

Whenever you see

Arrange

Seat

Line

Rank

Position

Immediately think

Permutation.

---

### Trick 6

Whenever you see

Choose

Select

Committee

Group

Team

Immediately think

Combination.

---

# 13. Basic Solved Examples

## Example 1

A person has

4 shirts

and

3 pants.

How many dress combinations are possible?

### Solution

Using Fundamental Principle

4×3

=

12

Answer

12

---

## Example 2

Find

5!

Solution

5!

=

5×4×3×2×1

=

120

Answer

120

---

## Example 3

Find

7!

÷5!

Solution

7!

=

7×6×5!

Cancel

5!

Answer

7×6

=

42

---

## Example 4

Is arranging

A

B

C

a Permutation or Combination?

Solution

Order changes.

Therefore,

Permutation.

---

## Example 5

Selecting

3 students

from a class

is a Permutation or Combination?

Solution

Only selection matters.

Therefore,

Combination.

---

## Example 6

A PIN consists of

4 digits.

Each digit can be chosen from

0–9.

How many possible PINs can be formed?

Solution

Each digit

10 choices

Using Fundamental Principle

10×10×10×10

=

10,000

Answer

10,000

---

# 14. Important Observations

Observe these carefully.

✔ Factorials are used extensively in counting problems.

✔ Arrangement always depends on order.

✔ Selection ignores order.

✔ Fundamental Principle of Counting is used whenever independent choices are multiplied.

✔ Permutation answers are generally greater than or equal to Combination answers for the same values of n and r.

✔ Before choosing a formula, always ask:

**"Does order matter?"**

---

# 15. Concept Check

Ask yourself.

✅ What is the Fundamental Principle of Counting?

✅ Can I calculate factorials?

✅ Do I know the properties of factorial?

✅ Can I identify Permutation problems?

✅ Can I identify Combination problems?

✅ Do I know when order matters?

If your answer is **Yes**,

you are ready to learn the actual **Permutation and Combination formulas**, circular arrangements, team selection problems, and placement-level shortcuts.

---

 # 16. Permutation Formula

After understanding the basic concept of arrangements,

we now learn the mathematical formula used to calculate permutations.

Permutation is used whenever

**Order Matters.**

---

## Formula

\[
^nP_r=\frac{n!}{(n-r)!}
\]

Where,

- n = Total number of objects
- r = Number of objects selected
- nPr = Number of Permutations

---

## Meaning of the Formula

Suppose,

5 students are available,

and only 3 positions are available.

Since positions matter,

we arrange them.

Number of arrangements

=

5P3

---

## Example 1

Find

5P2

### Solution

\[
^5P_2=\frac{5!}{3!}
\]

=

5×4

=

20

Answer

20

---

## Example 2

Find

6P3

Solution

6!

÷3!

=

6×5×4

=

120

Answer

120

---

## Example 3

Find

8P4

Solution

8!

÷4!

=

8×7×6×5

=

1680

Answer

1680

---

# 17. Combination Formula

Combination is used whenever

**Order Does Not Matter.**

---

## Formula

\[
^nC_r=\frac{n!}{r!(n-r)!}
\]

Where,

- n = Total Objects
- r = Objects Selected

---

## Example 1

Find

5C2

### Solution

\[
^5C_2=\frac{5!}{2!3!}
\]

=

5×4

÷2

=

10

Answer

10

---

## Example 2

Find

6C3

Solution

6!

÷(3!×3!)

=

720÷36

=

20

Answer

20

---

## Example 3

Find

8C4

Solution

8!

÷(4!×4!)

=

70

Answer

70

---

# 18. Why are nPr and nCr Different?

This is one of the most common interview questions.

Suppose

A

B

C

must fill

two positions.

---

### Permutation

AB

BA

Both are different.

Because positions changed.

---

### Combination

AB

Only one selection.

Order ignored.

---

Therefore,

Permutation

>

Combination

for the same

n

and

r.

---

## Observation

Permutation

=

Arrangement

Combination

=

Selection

---

# 19. Relationship Between Permutation and Combination

The formulas are connected.

## Formula

\[
^nP_r=^nC_r\times r!
\]

---

## Memory Trick

First

Select

↓

Then

Arrange

Selection

↓

Combination

Arrangement

↓

Permutation

---

## Example

Find

5P2

using

5C2

Solution

5C2

=

10

2!

=

2

Therefore,

5P2

=

10×2

=

20

Answer

20

---

# 20. Circular Permutation

Sometimes,

objects are arranged in a circle

instead of a straight line.

Examples

- Round Table
- Circular Garden
- Ferris Wheel
- Necklace
- Bracelet

---

## Formula

For

n

different objects,

Circular Arrangement

=

(n−1)!

---

## Why?

In a circle,

rotating everyone together

does not create a new arrangement.

Hence,

one position is fixed.

---

## Example 1

Arrange

5 people

around a round table.

Solution

(5−1)!

=

4!

=

24

Answer

24

---

## Example 2

Arrange

7 people

around a circular table.

Solution

6!

=

720

Answer

720

---

# 21. Permutation with Repetition Allowed

Sometimes,

objects can be repeated.

Example

Password

AAAA

is allowed.

---

## Formula

If

n choices

are available

for each position,

and

r positions exist,

Total Arrangements

=

n^r

---

## Example 1

A PIN contains

4 digits.

Each digit

0–9

can repeat.

Solution

10⁴

=

10,000

Answer

10,000

---

## Example 2

A password contains

3 uppercase letters.

Each letter can repeat.

Solution

26³

=

17,576

Answer

17,576

---

# 22. Permutation without Repetition

If repetition is not allowed,

use

nPr.

---

## Example

Arrange

3 letters

from

A

B

C

D

Solution

4P3

=

24

Answer

24

---

# 23. Arrangement of Letters

These questions appear frequently in placements.

---

## Example 1

Arrange the letters of

CAT.

Solution

3!

=

6

Answer

6

---

## Example 2

Arrange the letters of

BOOK.

Letters

O

repeats twice.

Formula

=

4!

÷2!

=

12

Answer

12

---

## Example 3

Arrange the letters of

LEVEL.

Letters

L

repeats twice.

E

repeats twice.

Formula

=

5!

÷(2!×2!)

=

30

Answer

30

---

# 24. Arrangement of Numbers

---

## Example 1

Arrange digits

1

2

3

4

Solution

4!

=

24

---

## Example 2

Arrange

5 different digits

taking

3 at a time.

Solution

5P3

=

60

---

## Example 3

How many

3-digit numbers

can be formed from

1

2

3

4

without repetition?

Solution

4P3

=

24

Answer

24

---

# 25. Selection Problems

Selection questions always use

Combination.

---

## Example 1

Choose

3 students

from

8 students.

Solution

8C3

=

56

Answer

56

---

## Example 2

Choose

2 captains

from

10 players.

Solution

10C2

=

45

Answer

45

---

# 26. Team Formation

Very common in placements.

---

## Example

A cricket team has

15 players.

Select

11 players.

Solution

15C11

=

15C4

=

1365

Answer

1365

---

## Important Property

\[
^nC_r=^nC_{n-r}
\]

Example

10C2

=

10C8

Both

=

45

This property reduces calculations.

---

# 27. Committee Problems

Committee questions always use

Combination.

---

## Example 1

A committee of

5 members

is to be selected

from

12 people.

Solution

12C5

=

792

Answer

792

---

## Example 2

A club has

20 members.

Select

4 members

for an event.

Solution

20C4

=

4845

Answer

4845

---

# 28. Advanced Solved Examples

---

## Example 1

How many

4-letter words

can be formed from

7 letters

without repetition?

Solution

7P4

=

840

Answer

840

---

## Example 2

How many committees of

3 members

can be formed

from

8 members?

Solution

8C3

=

56

Answer

56

---

## Example 3

How many ways can

6 people

sit around a circular table?

Solution

(6−1)!

=

120

Answer

120

---

## Example 4

Find

10P3

Solution

10×9×8

=

720

Answer

720

---

# 29. Shortcut Techniques

### Shortcut 1

Arrange

↓

Permutation

---

### Shortcut 2

Select

↓

Combination

---

### Shortcut 3

Circle

↓

(n−1)!

---

### Shortcut 4

Repeated Letters

↓

Divide by repeated factorials.

---

### Shortcut 5

Repeated Digits Allowed

↓

n^r

---

### Shortcut 6

Remember

nCr

=

nC(n−r)

This saves time.

---

### Shortcut 7

Cancel factorials immediately.

Never expand the whole factorial unnecessarily.

---

# 30. Common Mistakes

❌ Using Permutation when only selection is required.

❌ Forgetting to divide by repeated letters.

❌ Using

nPr

instead of

nCr.

❌ Forgetting Circular Arrangement formula.

❌ Expanding large factorials unnecessarily.

❌ Ignoring whether repetition is allowed.

---

# 31. Concept Check

Ask yourself.

✅ Can I calculate

nPr?

✅ Can I calculate

nCr?

✅ Do I know when to use Permutation?

✅ Do I know when to use Combination?

✅ Can I solve Circular Arrangement?

✅ Can I solve Repetition problems?

✅ Can I solve Letter Arrangement questions?

If your answer is **Yes**,

you are ready for placement-level Permutation and Combination questions.

---
 # 32. Advanced Placement Questions

The following questions are designed according to the pattern of

- TCS
- Infosys
- Accenture
- Capgemini
- Cognizant
- Deloitte
- Wipro
- IBM

The difficulty gradually increases from Medium to Advanced.

---

## Question 1

In how many ways can

5 students

stand in a line?

### Solution

Since the order matters,

use Permutation.

Number of Arrangements

=

5!

=

5×4×3×2×1

=

120

Answer

120

---

## Question 2

From

8 students,

a team of

3 students

is to be selected.

Find the number of possible teams.

### Solution

Only selection matters.

Use Combination.

8C3

=

8!

÷(3!×5!)

=

56

Answer

56

---

## Question 3

How many

4-digit numbers

can be formed from

1,2,3,4,5

without repetition?

### Solution

Order matters.

Use Permutation.

5P4

=

5!

÷1!

=

120

Answer

120

---

## Question 4

How many different arrangements can be made using the letters of

APPLE?

### Solution

Total Letters

5

Repeated Letter

P appears

2 times.

Formula

=

5!

÷2!

=

120÷2

=

60

Answer

60

---

## Question 5

In how many ways can

7 people

sit around a circular table?

### Solution

Circular Arrangement

=

(7−1)!

=

6!

=

720

Answer

720

---

## Question 6

A committee of

4 members

is to be formed from

10 people.

Find the number of possible committees.

### Solution

10C4

=

210

Answer

210

---

## Question 7

How many

3-letter words

can be formed from

A,B,C,D,E

without repetition?

### Solution

5P3

=

5×4×3

=

60

Answer

60

---

## Question 8

How many

5-digit numbers

can be formed using

0,1,2,3,4

without repetition?

### Solution

First digit cannot be zero.

Choices for first digit

=

4

Remaining digits

=

4!

=

24

Total Numbers

=

4×24

=

96

Answer

96

---

# 33. Restricted Arrangement Problems

These questions are frequently asked in placement exams.

The restriction changes the counting process.

---

## Case 1

Two Persons Always Together

### Example

In how many ways can

A,

B,

C,

D,

E

be arranged

if

A and B must always remain together?

### Solution

Treat

A and B

as one unit.

Units become

(AB)

C

D

E

Total Units

4

Arrange them

4!

=

24

Now,

A and B

can interchange.

2!

=

2

Total Arrangements

=

24×2

=

48

Answer

48

---

## Case 2

Two Persons Never Together

### Example

Find the number of arrangements of

A,B,C,D,E

if

A and B

never sit together.

### Solution

Total Arrangements

=

5!

=

120

Together

=

48

Required Answer

=

120−48

=

72

Answer

72

---

## Case 3

Vowels Always Together

### Example

Arrange

EDUCATION

such that all vowels remain together.

### Solution

Treat all vowels as one block.

Arrange the block with consonants.

Then arrange vowels inside the block.

This type of question follows the same block method.

---

# 34. Seating Arrangement Problems

These questions are common in campus placements.

---

## Example 1

Six friends sit in a row.

Find the number of arrangements.

Solution

6!

=

720

---

## Example 2

Eight friends sit around a circular table.

Solution

(8−1)!

=

7!

=

5040

---

## Example 3

Five boys and three girls sit in a row.

How many arrangements are possible?

Solution

8!

=

40320

---

# 35. Real-Life Applications

Permutation and Combination are used in many real-world fields.

---

## Password Generation

Example

A password contains

6 characters.

The total number of possible passwords can be calculated using counting principles.

---

## Lottery Systems

Lottery numbers are selected using combinations.

Order usually does not matter.

---

## Team Selection

Sports teams,

Project groups,

Interview panels,

Committees

All use combinations.

---

## Scheduling

Examination timetables,

Employee shifts,

Machine scheduling

often use permutations.

---

## Artificial Intelligence

Permutation and Combination are used in

- Search Algorithms
- State Space Exploration
- Game Playing
- Optimization Problems
- Genetic Algorithms

---

## Machine Learning

Applications include

- Feature Selection
- Cross Validation
- Hyperparameter Search
- Data Sampling

---

# 36. Company Pattern

Understanding company patterns helps in targeted preparation.

---

## TCS

Difficulty

Easy to Medium

Frequently Asked

- Basic Permutation
- Factorial
- Counting Principle

---

## Infosys

Medium

Frequently Asked

- Team Selection
- Committee Problems
- Circular Arrangement

---

## Accenture

Medium to Difficult

Frequently Asked

- Mixed Permutation & Combination
- Repeated Letters
- Restriction Problems

---

## Capgemini

Medium

Frequently Asked

- Selection Problems
- Word Arrangement

---

## Cognizant

Easy to Medium

Frequently Asked

- Formula-Based Questions
- Counting Principle

---

## Deloitte

Moderate

Frequently Asked

- Logical Arrangement
- Business Case Scenarios
- Selection Problems

---

# 37. Expert Shortcut Techniques

### Shortcut 1

Arrange

↓

Permutation

---

### Shortcut 2

Choose

↓

Combination

---

### Shortcut 3

Circle

↓

(n−1)!

---

### Shortcut 4

Repeated Letters

↓

Divide by repeated factorials.

---

### Shortcut 5

Two people together

↓

Treat them as one block.

---

### Shortcut 6

Two people never together

↓

Total Arrangements

−

Together Arrangements

---

### Shortcut 7

Use

nCr

=

nC(n−r)

to reduce calculations.

Example

15C13

↓

15C2

---

### Shortcut 8

Cancel factorials before expanding.

This saves both time and calculations.

---

# 38. Common Mistakes

Most students lose marks because of these mistakes.

---

### Mistake 1

Using

Permutation

instead of

Combination.

Always ask

**"Does order matter?"**

---

### Mistake 2

Expanding large factorials unnecessarily.

Cancel first.

Expand later.

---

### Mistake 3

Forgetting repeated letters.

Example

BALLOON

contains repeated letters.

Division by repeated factorials is required.

---

### Mistake 4

Using

n!

instead of

(n−1)!

for circular arrangements.

---

### Mistake 5

Ignoring restrictions.

Questions like

"Always together"

or

"Never together"

require different approaches.

---

### Mistake 6

Allowing zero as the first digit in number formation problems.

Numbers cannot begin with zero.

---

# 39. Interview Questions

### Q1

What is the difference between Permutation and Combination?

Permutation deals with arrangements where order matters.

Combination deals with selections where order does not matter.

---

### Q2

Why is

0!

equal to

1?

It is defined mathematically to keep factorial formulas consistent.

---

### Q3

Why is circular permutation

(n−1)!

instead of

n!?

Because rotating everyone together in a circle does not create a new arrangement.

One position is fixed to eliminate duplicate rotations.

---

### Q4

When should

nPr

be used?

Whenever order matters.

---

### Q5

When should

nCr

be used?

Whenever only selection matters.

---

### Q6

Why is

Permutation

always greater than or equal to

Combination?

Because every selection can be arranged in multiple different orders.

---

# 40. Formula Revision Sheet

## Fundamental Principle of Counting

Total Ways

=

Multiply all independent choices.

---

## Factorial

n!

=

n×(n−1)...×1

---

## Permutation

nPr

=

n!

÷(n−r)!

---

## Combination

nCr

=

n!

÷[r!(n−r)!]

---

## Relationship

nPr

=

nCr×r!

---

## Circular Permutation

(n−1)!

---

## Repetition Allowed

n^r

---

## Combination Property

nCr

=

nC(n−r)

---

# 41. One-Minute Revision

✔ Counting Principle

↓

Multiply choices.

✔ Factorial

↓

n!

✔ Arrangement

↓

Permutation.

✔ Selection

↓

Combination.

✔ Order Matters

↓

Permutation.

✔ Order Doesn't Matter

↓

Combination.

✔ Circular Arrangement

↓

(n−1)!

✔ Repeated Letters

↓

Divide by repeated factorials.

✔ Two Persons Together

↓

One Block.

✔ Two Persons Never Together

↓

Total−Together.

---

# 42. Self Assessment

Before moving to the next aptitude topic,

make sure you can confidently:

✅ Apply the Fundamental Principle of Counting.

✅ Calculate factorials quickly.

✅ Solve Permutation problems.

✅ Solve Combination problems.

✅ Identify when order matters.

✅ Solve circular arrangement questions.

✅ Solve repeated-letter problems.

✅ Solve committee and team selection problems.

✅ Solve restricted arrangement questions.

If every answer is

**Yes**,

you have mastered Permutation and Combination.

---

# 43. Practice Questions

## Basic

1. Arrange 6 students in a row.

2. Select 3 students from 9 students.

3. Find 7P3.

4. Find 8C2.

5. Arrange the letters of DOG.

---

## Intermediate

6. Arrange the letters of BALLOON.

7. Form a committee of 5 members from 12 people.

8. Arrange 8 people around a circular table.

9. Form 4-digit numbers from digits 1,2,3,4,5 without repetition.

10. How many teams of 6 can be selected from 15 players?

---

## Placement Level

11. Arrange the letters of MISSISSIPPI.

12. In how many ways can 8 people sit in a row if two specific persons always sit together?

13. In how many ways can 8 people sit in a row if two specific persons never sit together?

14. How many passwords of length 5 can be formed using uppercase English letters if repetition is allowed?

15. A class has 12 boys and 8 girls. In how many ways can a committee of 5 be formed containing exactly 3 boys and 2 girls?

---

# 44. Key Takeaways

• **Permutation** is used when **order matters**.

• **Combination** is used when **order does not matter**.

• The Fundamental Principle of Counting is the basis of all counting techniques.

• Factorials simplify complex counting problems.

• Circular arrangements require the formula **(n−1)!**.

• For repeated letters, divide by the factorial of each repeated letter count.

• Restricted arrangement problems often use the **block method** or the **Total − Restricted** approach.

• Permutation and Combination are fundamental concepts in Probability, Artificial Intelligence, Machine Learning, Cryptography, and Data Science.

---

# 45. Complete Chapter Summary

Congratulations!

You have successfully completed the **Permutation and Combination** chapter.

In this chapter, you learned:

- Fundamental Principle of Counting
- Factorials and Their Properties
- Permutation
- Combination
- Relationship Between nPr and nCr
- Circular Permutation
- Repetition Allowed and Not Allowed
- Arrangement of Letters
- Arrangement of Numbers
- Team Selection
- Committee Formation
- Restricted Arrangement Problems
- Seating Arrangement Concepts
- Company Pattern
- Interview Questions
- Formula Revision Sheet
- Practice Questions

You are now well prepared to solve Permutation and Combination questions asked in campus placements, banking examinations, competitive aptitude tests, and coding interviews.

---

 