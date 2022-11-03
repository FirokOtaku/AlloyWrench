package firok.tool.alloywrench.util;

import firok.tool.alloywrench.bean.IntRect;

import java.awt.image.BufferedImage;
import java.util.*;

public class Images
{
	/**
	 * 计算图片裁剪范围
	 *
	 * @param imageWidth 图片宽度
	 * @param imageHeight 图片高度
	 * @param pieceX 裁剪后每块宽度
	 * @param pieceY 裁剪后每块高度
	 * @param overlyingX 叠盖宽度
	 * @param overlyingY 叠盖高度
	 * */
	public static ArrayList<IntRect> calcCutRanges(
			int imageWidth, int imageHeight,
			final int pieceX, final int pieceY,
			final int overlyingX, final int overlyingY
	)
	{
		var listRange = new ArrayList<IntRect>();
		int fromX = 0, toX = pieceX;
		// 裁剪偏移量
		final int _cutOffsetX = pieceX - overlyingX;
		final int _cutOffsetY = pieceY - overlyingY;
		while(true)
		{
			final boolean isFinalX = toX >= imageWidth;

			if(imageWidth < toX)
			{
				toX = imageWidth;
				fromX = Math.max(0, imageWidth - pieceX);
			}

			int fromY = 0, toY = pieceY;
			while(true)
			{
				final boolean isFinalY = toY >= imageHeight;

				if(imageHeight < toY)
				{
					toY = imageHeight;
					fromY = Math.max(0, imageHeight - pieceY);
				}

				var range = new IntRect(fromX, fromY, toX, toY);
				listRange.add(range);

				if(isFinalY) break;

				fromY += _cutOffsetY;
				toY = fromY + pieceY;
			}

			if(isFinalX) break;

			fromX += _cutOffsetX;
			toX = fromX + pieceX;
		}
		return listRange;
	}

	/**
	 * 获取图片某范围内的子图片
	 * */
	public static BufferedImage cutImage(BufferedImage image, IntRect rect)
	{
		return image.getSubimage(
				rect.fromX(),
				rect.fromY(),
				rect.toX() - rect.fromX(),
				rect.toY() - rect.fromY()
		);
	}
}
