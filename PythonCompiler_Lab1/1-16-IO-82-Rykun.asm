.386
.model flat,stdcall
option casemap:none

include     C:\masm32\include\windows.inc
include     C:\masm32\include\kernel32.inc
include     C:\masm32\include\masm32.inc
includelib  C:\masm32\lib\kernel32.lib
includelib  C:\masm32\lib\masm32.lib

_NumbToStr   PROTO :DWORD,:DWORD
_main        PROTO

func1	PROTO
func2	PROTO

.data
buff        db 11 dup(?)

.code
_start:
	invoke  _main
	invoke  _NumbToStr, ebx, ADDR buff
	invoke  StdOut,eax
	invoke  ExitProcess,0

_main PROC

	call func1
	call func2

	ret

_main ENDP

func1 PROC
mov ebx, 10
ret
func1 ENDP
func2 PROC
mov ebx, 21
ret
func2 ENDP

_NumbToStr PROC uses ebx x:DWORD,buffer:DWORD

	mov     ecx,buffer
	mov     eax,x
	mov     ebx,10
	add     ecx,ebx
@@:
	xor     edx,edx
	div     ebx
	add     edx,48
	mov     BYTE PTR [ecx],dl
	dec     ecx
	test    eax,eax
	jnz     @b
	inc     ecx
	mov     eax,ecx
	ret

_NumbToStr ENDP

END _start
