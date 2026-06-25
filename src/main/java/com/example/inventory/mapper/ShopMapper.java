package com.example.inventory.mapper;
import com.example.inventory.model.Shop; import java.util.List;
public interface ShopMapper { List<Shop> findAll(); Shop findByCode(String shopCode); int insert(Shop shop); int update(Shop shop); int delete(String shopCode); int countStorage(String shopCode); }
