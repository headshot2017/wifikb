/*
	Test program for the wifikb library
	by headshot2017
*/

#include <stdio.h>
#include <string.h>
#include <netinet/in.h>

#include <nds.h>
#include <dswifi9.h>

#include "wifikb/wifikb.h"

int main(int argc, char *argv[])
{
	PrintConsole topScreen;
	PrintConsole bottomScreen;

	videoSetMode(MODE_0_2D);
	videoSetModeSub(MODE_0_2D);

	vramSetBankA(VRAM_A_MAIN_BG);
	vramSetBankC(VRAM_C_SUB_BG);

	consoleInit(&topScreen, 3, BgType_Text4bpp, BgSize_T_256x256, 31, 0, true, true);
	consoleInit(&bottomScreen, 3, BgType_Text4bpp, BgSize_T_256x256, 31, 0, false, true);

	consoleSelect(&topScreen);

	printf("Connecting to WiFi using\n");
	printf("firmware settings...\n\n");

	if (!Wifi_InitDefault(WFC_CONNECT))
	{
		printf("Can't connect to WiFi!\n");
		while (1) swiWaitForVBlank();
	}
	else
	{
		printf("Connected!\n");

		struct in_addr ip, gateway, mask, dns1, dns2;
		ip = Wifi_GetIPInfo(&gateway, &mask, &dns1, &dns2);

		printf("\n");
		printf("Connection information:\n");
		printf("\n");
		printf("IP:      %s\n", inet_ntoa(ip));
		printf("Gateway: %s\n", inet_ntoa(gateway));
		printf("Mask:    %s\n", inet_ntoa(mask));
		printf("DNS1:    %s\n", inet_ntoa(dns1));
		printf("DNS2:    %s\n", inet_ntoa(dns2));
		printf("\n");
	}

	printf("Enable reverse connection mode?\n");
	printf("This allows connecting to\n");
	printf("emulators.\n");
	printf("A: Yes\n");
	printf("B: No\n");

	while (1)
	{
		swiWaitForVBlank();

		scanKeys();
		u32 key = keysDown();

		if (key & (KEY_A | KEY_B))
		{
			wifikb::setReverse(!!(key & KEY_A));
			printf("Reverse mode %sabled\nWaiting for connection now\n", key&KEY_A ? "en" : "dis");
			break;
		}
	}

	consoleSelect(&bottomScreen);
	wifikb::init();

	wifikb::start();

	while (1)
	{
		swiWaitForVBlank();
		wifikb::update();

		s32 key;
		if (wifikb::getKey(&key) && key >= 0)
			printf("%c", key);
	}

	return 0;
}