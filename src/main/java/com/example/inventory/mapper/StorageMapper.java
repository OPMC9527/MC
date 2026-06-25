package com.example.inventory.mapper;
import com.example.inventory.model.Storage; import java.util.List;
public interface StorageMapper { List<Storage> findAll(); Storage findByShopCode(String shopCode); int insert(Storage storage); int increase(String shopCode, int num); int decrease(String shopCode, int num); }
