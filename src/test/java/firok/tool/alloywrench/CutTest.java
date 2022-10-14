package firok.tool.alloywrench;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CutTest
{
	// 裁剪偏移量
	private static final int overlyingX = 200;
	private static final int overlyingY = 200;
	// 裁剪之后的大小
	private static final int pieceX = 1000;
	private static final int pieceY = 1000;
	// 裁剪偏移量
	private static final int cutOffsetX = pieceX - overlyingX;
	private static final int cutOffsetY = pieceY - overlyingY;

	void testOne(int shouldCountX, int shouldCountY, int imageWidth, int imageHeight)
	{
		int fromX = 0, toX = pieceX;
		int countX = 0, countY = 0;
		while(true)
		{
			final boolean isFinalX = toX >= imageWidth;

			if(imageWidth < toX)
			{
				toX = imageWidth;
				fromX = Math.max(0, imageWidth - pieceX);
			}

			countX++;

			int fromY = 0, toY = pieceY;
			while(true)
			{
				final boolean isFinalY = toY >= imageHeight;

				if(imageHeight < toY)
				{
					toY = imageHeight;
					fromY = Math.max(0, imageHeight - pieceY);
				}

				if(countX == 1)
				countY++;

				if(isFinalY) break;

				fromY += cutOffsetY;
				toY = fromY + pieceY;
			}

			if(isFinalX) break;

			fromX += cutOffsetX;
			toX = fromX + pieceX;
		}
		System.out.printf("should [%d, %d] count [%d, %d] image [%d, %d] \n",
				shouldCountX, shouldCountY,
				countX, countY,
				imageWidth, imageHeight
		);
		Assertions.assertEquals(shouldCountX, countX);
		Assertions.assertEquals(shouldCountY, countY);
	}
	@Test
	void test()
	{
		testOne(1, 1, 400, 400);
		testOne(1, 1, 800, 800);
		testOne(1, 1, 801, 801);
		testOne(1, 1, 1000, 1000);
		testOne(2, 2, 1001, 1001);
		testOne(2, 2, 1800, 1800);
		testOne(3, 3, 2000, 2000);
		testOne(3, 3, 2001, 2001);
		testOne(3, 3, 2600, 2600);
		testOne(4, 4, 2601, 2601);
	}
}
